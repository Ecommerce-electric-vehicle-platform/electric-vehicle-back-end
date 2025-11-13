package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.enumerate.DisputeDecision;
import Green_trade.green_trade_platform.enumerate.SystemWalletStatus;
import Green_trade.green_trade_platform.mapper.DisputeMapper;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.model.*;
import Green_trade.green_trade_platform.request.MailRequest;
import Green_trade.green_trade_platform.request.RaiseDisputeRequest;
import Green_trade.green_trade_platform.request.RefundResolveRequest;
import Green_trade.green_trade_platform.request.ResolveDisputeRequest;
import Green_trade.green_trade_platform.response.DisputeResponse;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.service.implement.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/dispute")
@Slf4j
@AllArgsConstructor
@Tag(name = "Dispute Management", description = "APIs for managing order disputes, evidence, and resolutions")
public class DisputeController {
    private final DisputeServiceImpl disputeService;
    private final EvidenceServiceImpl evidenceService;
    private final DisputeMapper disputeMapper;
    private final ResponseMapper responseMapper;
    private final NotificationServiceImpl notificationService;
    private final AdminServiceImpl adminService;
    private final NotificationSocketController notificationSocketController;
    private final SystemWalletServiceImpl systemWalletService;
    private final WalletServiceImpl walletService;
    private final MailServiceImpl mailSender;

    @Operation(
            summary = "Raise a dispute for an order",
            description = """
                    Allows a buyer to submit a dispute related to an order. 
                    The API receives dispute details and evidence pictures, 
                    saves them to the database, 
                    updates the dispute with associated evidences, 
                    and sends a notification to the seller about the disputed product.
                    
                    **Validation Rules:**
                    - Only completed orders can have disputes
                    - If an order already has a pending dispute, a new dispute cannot be raised
                    - If all previous disputes have been resolved (ACCEPTED or REJECTED), a new dispute can be raised
                    """,
            tags = {"Dispute Management"},
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dispute raised successfully",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "RAISE DISPUTE SUCCESSFULLY",
                                              "data": {
                                                "disputeId": 1,
                                                "orderId": 123,
                                                "category": "PRODUCT_DEFECT",
                                                "description": "Product received is damaged",
                                                "status": "PENDING",
                                                "evidences": ["https://cloudinary.com/evidence1.jpg"]
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Validation failed or order has pending dispute",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": false,
                                              "message": "RAISE DISPUTE FAILED: Order already has pending dispute",
                                              "data": null,
                                              "error": "Order already has pending dispute"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found"
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER')")
    @PostMapping("/raise-dispute")
    public ResponseEntity<RestResponse<DisputeResponse, Object>> raiseDispute(
            @Parameter(description = "Dispute request with order ID, category, and description", required = true)
            @ModelAttribute RaiseDisputeRequest request,
            @Parameter(description = "Evidence pictures (images) supporting the dispute", required = true)
            @RequestPart("pictures") List<MultipartFile> files
    ) throws Exception {
        try {
            Dispute newDispute = disputeService.receiveDispute(request);
            List<Evidence> evidences = evidenceService.saveEvidence(files, newDispute);

            newDispute = disputeService.updateEvidencesForDispute(evidences, newDispute);
            log.info(">>> Passed update evidences for dispute");
            DisputeResponse responseData = disputeMapper.toDto(newDispute);
            RestResponse<DisputeResponse, Object> response = responseMapper.toDto(
                    true,
                    "RAISE DISPUTE SUCCESSFULLY",
                    responseData,
                    null
            );
            log.info(">>> Passed create response");
            return ResponseEntity.status(HttpStatus.OK.value()).body(response);
        } catch (Exception e) {
            log.info(">>> Error at raiseDispute: {}", e.getMessage());
            // Trả về error response thay vì throw exception
            RestResponse<DisputeResponse, Object> errorResponse = responseMapper.toDto(
                    false,
                    "RAISE DISPUTE FAILED: " + e.getMessage(),
                    null,
                    e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(errorResponse);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Retrieve pending disputes for decision-making",
            description = """
                        This endpoint returns a list of disputes that are currently pending review. 
                        Each dispute contains relevant information such as dispute ID, order details, 
                        evidence, and submission date.
                    
                        The retrieved disputes are those that have not yet been decided (accepted or rejected). 
                        Authorized users can review these disputes and proceed to make a decision 
                        by calling the corresponding Accept or Reject endpoints.
                    
                        Typical use cases:
                        - Admin dashboard fetching disputes awaiting approval
                        - Automated workflow checking pending disputes for manual intervention
                    
                        **Permissions:** Requires admin or dispute-manager role.
                    """,
            tags = {"Dispute Management"},
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pending disputes retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Admin role required"
            )
    })
    @GetMapping("")
    public ResponseEntity<?> getDisputes(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        log.info(">>> [Dispute controller]: getDisputes");
        try {
            Page<DisputeResponse> disputes = disputeService.getAllDispute(page, size);
            Map<String, Object> data = new HashMap<>();
            data.put("dispute", disputes.getContent());
            data.put("currentPage", disputes.getNumber());
            data.put("totalElements", disputes.getTotalElements());
            data.put("totalPages", disputes.getTotalPages());
            log.info(">>> Get disputes successfully: {}", disputes.getTotalElements());

            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET ALL PENDING DISPUTE SUCCESSFULLY.",
                    data, null));
        } catch (Exception e) {
            log.info(">>> Exception occur getDisputes: {}", e.getMessage());
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "GET ALL PENDING DISPUTE FAILED.",
                    null, e));
        }
    }

    @Operation(
            summary = "Admin makes a decision for a specific dispute",
            description = """
                        Allows an administrator or dispute manager to make a final decision on a dispute case.
                    
                        The frontend sends a request containing the following parameters:
                        - **disputeId** *(Long)* – Unique identifier of the dispute to be resolved.
                        - **decision** *(Enum (ACCEPTED or REJECTED))* – The final decision.
                        - **resolution** *(Description for resolving a dispute)* – Description or reasoning provided by the admin for the decision.
                        - **resolutionType** *(Enum (REFUND or REJECTED(in case the dispute is rejected)))* – Category or type of resolution.
                        - **refundPercent** *(double)* – Percentage of refund to be issued (if applicable).
                    
                        This endpoint updates the dispute status and triggers the corresponding 
                        post-decision workflow such as refund processing or notification dispatch.
                    
                        **Permissions:** Admin or dispute-manager role required.
                        **Example:** 
                        ```
                        POST /api/disputes/decision
                        {
                            "disputeId": "1",
                            "decision": "ACCEPTED",
                            "resolution": "Customer provided valid proof; refund approved.",
                            "resolutionType": "REFUND",
                            "refundPercent": 80
                        }
                        ```
                    """,
            tags = {"Dispute Management"},
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dispute resolved successfully",
                    content = @Content(schema = @Schema(implementation = Notification.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid request data"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Admin role required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Dispute not found"
            )
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/resolve")
    public ResponseEntity<?> handleDispute(
            @Parameter(description = "Dispute resolution request with decision and refund details", required = true)
            @RequestBody ResolveDisputeRequest request) {
        log.info(">>> [Dispute Controller]: Started.");
        try {
            log.info(">>> [Dispute Controller] Refund percent: {}", request.getRefundPercent());
            Admin admin = adminService.getCurrentUser();
            Notification notification = disputeService.handlePendingDispute(admin, request);
            Map<String, Object> orderTemp = disputeService.getOrderByDisputeId(request.getDisputeId());
            Order order = (Order) orderTemp.get("order");

            SystemWallet systemWallet = systemWalletService.getSystemWalletByOrder(order);
            log.info(">>> [Dispute Controller] System wallet information: {}", systemWallet);

            if (request.getDecision() == DisputeDecision.ACCEPTED) {
                Wallet buyerWallet = walletService.findWalletById(systemWallet.getBuyerWalletId());
                Wallet sellerWallet = walletService.findWalletById(systemWallet.getSellerWalletId());

                Wallet buyerWalletAfterRefund = walletService.handleBuyerRefund(systemWallet, request.getRefundPercent(), buyerWallet, false, order.getOrderCode());
                Wallet sellerWalletAfterRefund = walletService.handleBuyerRefund(systemWallet, 100 - request.getRefundPercent(), sellerWallet, true, order.getOrderCode());

                systemWalletService.handleRefund(systemWallet);

                MailRequest buyerMailRequest = MailRequest.builder()
                        .from("green.trade.platform.391@gmail.com")
                        .to(buyerWallet.getBuyer().getEmail())
                        .subject("Kết quả xử lý khiếu nại đơn hàng #" + order.getId())
                        .message("""
                                💚 <strong>Kết quả xử lý khiếu nại</strong><br><br>
                                Khiếu nại của bạn đã được Green Trade xem xét và phê duyệt hoàn tiền.<br>
                                Phần trăm hoàn tiền: %s%%<br><br>
                                Vui lòng kiểm tra ví của bạn để xác nhận giao dịch hoàn tiền đã được xử lý.<br><br>
                                Cảm ơn bạn đã tin tưởng Green Trade Platform!
                                """.formatted(request.getRefundPercent()))
                        .build();

                MailRequest sellerMailRequest = MailRequest.builder()
                        .from("green.trade.platform.391@gmail.com")
                        .to(sellerWallet.getBuyer().getEmail())
                        .subject("Kết quả khiếu nại đơn hàng #" + order.getId())
                        .message("""
                                ⚠️ <strong>Thông báo kết quả khiếu nại</strong><br><br>
                                Khiếu nại liên quan đến <strong>đơn hàng #%s</strong> đã được xử lý.<br><br>
                                🔹 <strong>Tỷ lệ thanh toán giữ lại:</strong> %s%%<br><br>
                                Vui lòng kiểm tra ví của bạn để xác nhận số dư mới.<br><br>
                                Trân trọng,<br>Green Trade Platform
                                """.formatted(order.getId(), 100 - request.getRefundPercent()))
                        .build();
                mailSender.sendBeautifulMail(sellerMailRequest);
                mailSender.sendBeautifulMail(buyerMailRequest);
            }
            notificationSocketController.sendNotificationToUser(notification);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_BUYER', 'ROLE_SELLER')")
    @Operation(
            summary = "Check if order has pending dispute",
            description = """
                        Checks whether a specific order currently has any pending disputes.
                    
                        This endpoint returns a simple boolean status indicating:
                        - true: The order has pending disputes
                        - false: The order does not have any pending disputes
                    
                        **Use Cases:**
                        - Check if an order can have a new dispute raised
                        - Validate before allowing dispute creation
                        - Display dispute status in order details
                    
                        **Path Parameters:**
                        - **orderId** *(Long, required)* - The unique identifier of the order
                    
                        **Response:**
                        Returns a simple response with:
                        - success: Boolean indicating if the check was successful
                        - message: Status message
                        - data: Boolean value (true if has pending dispute, false otherwise)
                    
                        **Permissions:** Admin, Buyer, or Seller roles required.
                        **Example:** GET /api/v1/dispute/order/123/pending-status
                    """
    )
    @GetMapping("/order/{orderId}/pending-status")
    public ResponseEntity<?> checkPendingDisputeByOrderId(@PathVariable(name = "orderId") Long orderId) {
        log.info(">>> [Dispute Controller] Check pending dispute for orderId: {}", orderId);
        try {
            boolean hasPending = disputeService.hasPendingDisputeByOrderId(orderId);
            String message = hasPending
                    ? "Order has pending dispute(s). Cannot raise new dispute."
                    : "Order does not have pending dispute. Can raise new dispute.";
            return ResponseEntity.ok(responseMapper.toDto(true,
                    message,
                    hasPending,
                    null));
        } catch (Exception e) {
            log.error(">>> [Dispute Controller] Error checking pending dispute: {}", e.getMessage());
            return ResponseEntity.ok(responseMapper.toDto(false,
                    "CHECK PENDING DISPUTE STATUS FAILED: " + e.getMessage(),
                    null,
                    e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_BUYER', 'ROLE_SELLER')")
    @Operation(
            summary = "Get all disputes by order ID",
            description = """
                        Retrieves all disputes associated with a specific order.
                    
                        This endpoint returns a list of disputes that have been raised for the given order.
                        Since an order can have multiple disputes (e.g., different dispute categories),
                        the response is a list of dispute objects.
                    
                        **Use Cases:**
                        - View all disputes related to a specific order
                        - Check dispute history for an order
                        - Admin reviewing order-related disputes
                    
                        **Path Parameters:**
                        - **orderId** *(Long, required)* - The unique identifier of the order
                    
                        **Response:**
                        Returns a list of dispute objects, each containing:
                        - Dispute ID, status, creation date
                        - Dispute category and description
                        - Evidence and resolution details
                        - Related order information
                    
                        **Permissions:** Admin, Buyer, or Seller roles required.
                        **Example:** GET /api/v1/dispute/order/123
                    """
    )
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getDisputesByOrderId(@PathVariable(name = "orderId") Long orderId) {
        log.info(">>> [Dispute Controller] Get disputes by orderId: {}", orderId);
        try {
            List<DisputeResponse> disputes = disputeService.getDisputesByOrderId(orderId);
            return ResponseEntity.ok(responseMapper.toDto(true,
                    "GET DISPUTES BY ORDER ID SUCCESSFULLY.",
                    disputes,
                    null));
        } catch (Exception e) {
            log.error(">>> [Dispute Controller] Error getting disputes by orderId: {}", e.getMessage());
            return ResponseEntity.ok(responseMapper.toDto(false,
                    "GET DISPUTES BY ORDER ID FAILED: " + e.getMessage(),
                    null,
                    e));
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_BUYER', 'ROLE_SELLER')")
    @Operation(
            summary = "Get detailed information of a specific dispute",
            description = """
                        Retrieves complete details of a dispute based on its unique identifier.
                    
                        The response includes:
                        - Dispute metadata (ID, status, creation date)
                        - Related order and transaction details
                        - Evidence and comments provided by buyer
                    
                        This endpoint is typically used by administrators 
                        to review the full context of a dispute before making a resolution decision.
                    
                        **Permissions:** Requires admin or dispute-manager privileges.
                        **Example:** GET /api/disputes/{disputeId}
                    """
    )
    @GetMapping("/{disputeId}")
    public ResponseEntity<?> getDisputeInfo(@PathVariable(name = "disputeId") long disputeId) {
        Dispute dispute = disputeService.getDisputeInfo(disputeId);
        return ResponseEntity.ok(responseMapper.toDto(true,
                "GET DISPUTE INFOR SUCCESSFULLY.",
                disputeMapper.toDto(dispute),
                null));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_BUYER', 'ROLE_SELLER')")
    @Operation(
            summary = "Get all disputes by buyer ID",
            description = """
                        Retrieves a paginated list of disputes raised by a specific buyer.
                    
                        This endpoint returns all disputes associated with orders belonging to the specified buyer.
                        The results are paginated and sorted by creation date (newest first).
                    
                        **Use Cases:**
                        - View all disputes raised by a buyer
                        - Check dispute history for a specific user
                        - Admin reviewing buyer-related disputes
                        - Buyer viewing their own dispute history
                    
                        **Path Parameters:**
                        - **buyerId** *(Long, required)* - The unique identifier of the buyer
                    
                        **Query Parameters:**
                        - **page** *(int, optional)* - Page number (default: 0)
                        - **size** *(int, optional)* - Page size (default: 10)
                    
                        **Response:**
                        Returns a paginated list of dispute objects, each containing:
                        - Dispute ID, status, creation date
                        - Dispute category and description
                        - Evidence and resolution details
                        - Related order information
                        - Refund information (if resolved)
                    
                        **Permissions:** Admin, Buyer, or Seller roles required.
                        **Example:** GET /api/v1/dispute/buyer/123?page=0&size=10
                    """
    )
    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<?> getDisputesByBuyerId(
            @PathVariable(name = "buyerId") Long buyerId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        log.info(">>> [Dispute Controller] Get disputes by buyerId: {}", buyerId);
        try {
            Page<DisputeResponse> disputes = disputeService.getDisputesByBuyerId(buyerId, page, size);

            Map<String, Object> data = new HashMap<>();
            data.put("disputes", disputes.getContent());
            data.put("currentPage", disputes.getNumber());
            data.put("totalElements", disputes.getTotalElements());
            data.put("totalPages", disputes.getTotalPages());

            return ResponseEntity.ok(responseMapper.toDto(true,
                    "GET DISPUTES BY BUYER ID SUCCESSFULLY.",
                    data,
                    null));
        } catch (Exception e) {
            log.error(">>> [Dispute Controller] Error getting disputes by buyerId: {}", e.getMessage());
            return ResponseEntity.ok(responseMapper.toDto(false,
                    "GET DISPUTES BY BUYER ID FAILED: " + e.getMessage(),
                    null,
                    e));
        }
    }
}
