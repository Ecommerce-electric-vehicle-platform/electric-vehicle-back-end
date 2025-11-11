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
            description = "Allows a buyer to submit a dispute related to an order. " +
                    "The API receives dispute details and evidence pictures, " +
                    "saves them to the database, " +
                    "updates the dispute with associated evidences, " +
                    "and sends a notification to the seller about the disputed product."
    )
    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER')")
    @PostMapping("/raise-dispute")
    public ResponseEntity<RestResponse<DisputeResponse, Object>> raiseDispute(
            @ModelAttribute RaiseDisputeRequest request,
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
            log.info(">>> Error at raiseDisput: {}", e.getMessage());
            throw e;
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
                    """
    )
    @GetMapping("")
    public ResponseEntity<?> getDisputes(
            @RequestParam(name = "page", defaultValue = "0") int page,
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
                    """
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/resolve")
    public ResponseEntity<?> handleDispute(@RequestBody ResolveDisputeRequest request) {
        log.info(">>> [Dispute Controller]: Started.");
        try {
            Admin admin = adminService.getCurrentUser();
            Notification notification = disputeService.handlePendingDispute(admin, request);
            Map<String, Object> orderTemp = disputeService.getOrderByDisputeId(request.getDisputeId());
            Order order = (Order) orderTemp.get("order");

            SystemWallet systemWallet = systemWalletService.getSystemWalletByOrder(order);
            log.info(">>> [Dispute Controller] System wallet information: {}", systemWallet);

            if (request.getDecision() == DisputeDecision.ACCEPTED) {
                Wallet buyerWallet = walletService.findWalletById(systemWallet.getBuyerWalletId());
                Wallet sellerWallet = walletService.findWalletById(systemWallet.getSellerWalletId());

                Wallet buyerWalletAfterRefund = walletService.handleBuyerRefund(systemWallet, request.getRefundPercent(), buyerWallet, false);
                Wallet sellerWalletAfterRefund = walletService.handleBuyerRefund(systemWallet, 100 - request.getRefundPercent(), sellerWallet, true);

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
}
