package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.AdminMapper;
import Green_trade.green_trade_platform.mapper.PostProductListMapper;
import Green_trade.green_trade_platform.mapper.PostProductMapper;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.model.Admin;
import Green_trade.green_trade_platform.model.Notification;
import Green_trade.green_trade_platform.model.PostProduct;
import Green_trade.green_trade_platform.model.Seller;
import Green_trade.green_trade_platform.request.ApproveSellerRequest;
import Green_trade.green_trade_platform.request.CreateAdminRequest;
import Green_trade.green_trade_platform.request.NeedVerifyPostRequest;
import Green_trade.green_trade_platform.request.PostProductDecisionRequest;
import Green_trade.green_trade_platform.response.*;
import Green_trade.green_trade_platform.service.implement.AdminServiceImpl;
import Green_trade.green_trade_platform.service.implement.PostProductServiceImpl;
import Green_trade.green_trade_platform.service.implement.SellerServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    private final SellerServiceImpl sellerService;
    private final PostProductServiceImpl postProductServiceImpl;
    private final ResponseMapper responseMapper;
    private final AdminServiceImpl adminService;
    private final AdminMapper adminMapper;
    private final PostProductMapper postProductMapper;
    private final PostProductListMapper postProductListMapper;
    private final NotificationSocketController socketController;

    @Operation(
            summary = "Get all pending seller accounts",
            description = """
        Retrieves a paginated list of seller accounts that are currently in a pending verification or approval state.
        This endpoint is restricted to administrators only (requires ROLE_ADMIN authority).

        The API supports pagination using the 'page' and 'size' query parameters.

        Response includes:
        - A list of sellers awaiting approval (`sellers`)
        - Pagination details such as current page, total elements, and total pages

        Typical use cases:
        - Admin dashboard for managing seller approvals
    """
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/pending-seller")
    public ResponseEntity<?> findAllPendingSeller(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<SellerResponse> ans = sellerService.getAllPendingSeller(page, size);
        Map<String, Object> body = new HashMap<>();
        body.put("sellers", ans.getContent());
        body.put("currentPage", ans.getNumber());
        body.put("totalElements", ans.getTotalElements());
        body.put("totalPage", ans.getTotalPages());

        return ResponseEntity.ok(body);
    }

    @Operation(
            summary = "Approve or reject a pending seller account",
            description = """
        Handles the approval or rejection process for a pending seller registration request. 
        This endpoint is restricted to administrators and requires a valid bearer token.

        The request body should contain seller information along with an approval decision. 
        If approved, the seller's account status is updated and a notification is sent to the user 
        in real time via WebSocket.

        **Workflow:**
        1. Admin submits approval/rejection data through this endpoint.
        2. The system updates the seller’s status.
        3. A notification is constructed and timestamped (`sendAt`).
        4. The notification is sent to the corresponding seller user through a socket event.

        **Use cases:**
        - Approving verified sellers after document validation.
        - Rejecting invalid or incomplete seller registration requests.
    """
    )
    @PostMapping("/approve-seller")
    public ResponseEntity<RestResponse<?, ?>> handlePendingSeller(@RequestBody ApproveSellerRequest request) throws JsonProcessingException {
        ApproveSellerResponse sellerNotification = sellerService.handlePendingSeller(request);
        sellerNotification.getNotification().setSendAt(LocalDateTime.now());
        socketController.sendUpgradeNotificationToUser(sellerNotification);
        return ResponseEntity.ok(responseMapper.toDto(true,
                "Approve request was be solved.",
                sellerNotification, null));
    }

    @Operation(
            summary = "Create a new admin account",
            description = """
        Allows an existing administrator to create a new admin account in the system.
        This endpoint accepts both form data and a profile image file (`avatar_url`) for the new admin.

        The request must include valid admin details (username, email, password, role, etc.) 
        and an avatar image. The uploaded avatar will be processed and linked to the new account.

        **Workflow:**
        1. Admin submits a multipart/form-data request containing admin details and an avatar image.
        2. The system validates the request and saves the image.
        3. The new admin account is created and persisted in the database.
        4. A success response is returned with the created admin's information.

        **Use cases:**
        - Registering additional admin users for system management.
        - Managing multi-admin access in the platform.
    """
    )
    @PostMapping("creating-admin")
    public ResponseEntity<?> handleCreatingAdmin(
            @Valid @ModelAttribute CreateAdminRequest request,
            @RequestPart(value = "avatar_url", required = true)MultipartFile avatarFile
    ) {
       try {
           Admin data = adminService.handleCreateAdminAccount(avatarFile, request);
           return ResponseEntity.ok(responseMapper.toDto(true,
                   "Create admin account successfully.",
                   adminMapper.toDto(data),
                   null));
       } catch (Exception e) {
           return ResponseEntity.ok(responseMapper.toDto(false,
                   "Create admin account failed.",
                   null,
                   e));
       }

    }

//    @PreAuthorize("hasRole('ROLE_SELLER')")
    @Operation(summary = "Review Post Product List API",
            description = "Return a post product list")
    @GetMapping("/review-post-seller-list")
    public ResponseEntity<RestResponse<PostProductListResponse, Object>> getAllPostProductForReview(
            @RequestParam(name = "size",defaultValue = "10") int size,
            @RequestParam(name = "page",defaultValue = "0") int page
    ) throws Exception {
        log.info(">>> Server came getAllPostProductForReview API");
        Page<PostProduct> postProducts = postProductServiceImpl.getAllPostProductForVerifiedReview(size, page);
        log.info(">>> Server ran postProductServiceImpl.getAllPostProduct()");

        Map<String, Object> meta = Map.of(
                "currentPage", postProducts.getNumber(),
                "totalElements", postProducts.getTotalElements(),
                "totalPage", postProducts.getTotalPages()
        );

        PostProductListResponse responseData = postProductListMapper.toDto(postProducts.getContent(), meta);

        RestResponse<PostProductListResponse, Object> response = responseMapper.toDto(
                true,
                "POST PRODUCT LIST",
                responseData,
                null
        );

        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

//    @PreAuthorize("hasRole('ROLE_SELLER')")
    @Operation(summary = "View Post Details For Admin Review API",
            description = "Return post product detail")
    @GetMapping("/{postProductId}/post-details")
    public ResponseEntity<RestResponse<PostProductResponse, Object>> viewPostProductDetail(
            @PathVariable Long postProductId
    ) throws Exception {
        PostProduct postProduct = postProductServiceImpl.getPostProductById(postProductId);
        PostProductResponse responseData = postProductMapper.toDto(postProduct);
        RestResponse<PostProductResponse, Object> response = responseMapper.toDto(
                true,
                "POST PRODUCT DETAIL",
                responseData,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

//    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Decide Post Product Verified API",
            description = "Return a result show that post product decision")
    @PostMapping("/review-post-product-decision")
    public ResponseEntity<RestResponse<PostProductResponse, Object>> reviewPostProductDecision(@Valid @RequestBody PostProductDecisionRequest request) throws Exception {
        PostProduct result = postProductServiceImpl.checkPostProductVerification(request);
        PostProductResponse responseData = postProductMapper.toDto(result);
        RestResponse response = responseMapper.toDto(
                true,
                "POST HAS BEEN CHECKED",
                responseData,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }


}
