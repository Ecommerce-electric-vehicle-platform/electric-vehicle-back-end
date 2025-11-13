package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.enumerate.VerifiedDecisionStatus;
import Green_trade.green_trade_platform.mapper.*;
import Green_trade.green_trade_platform.model.*;
import Green_trade.green_trade_platform.request.*;
import Green_trade.green_trade_platform.response.*;
import Green_trade.green_trade_platform.service.implement.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import Green_trade.green_trade_platform.util.DateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Management", description = "APIs for admin operations including seller approval, product review, user management, system wallet, and subscription package management")
public class AdminController {
    private final SellerServiceImpl sellerService;
    private final PostProductServiceImpl postProductServiceImpl;
    private final ResponseMapper responseMapper;
    private final AdminServiceImpl adminService;
    private final AdminMapper adminMapper;
    private final PostProductMapper postProductMapper;
    private final PostProductListMapper postProductListMapper;
    private final NotificationSocketController socketController;
    private final BuyerServiceImpl buyerService;
    private final PostProductServiceImpl postProductService;
    private final MailServiceImpl mailSender;
    private final SystemWalletServiceImpl systemWalletService;
    private final SystemWalletMapper systemWalletMapper;
    private final SubscriptionPackageServiceImpl subscriptionPackageService;
    private final SubscriptionPackageMapper subscriptionPackageMapper;

    @Operation(
            summary = "Get all pending seller accounts",
            description = """
                        Retrieves a paginated list of seller accounts that are currently in a pending verification or approval state.
                        This endpoint is restricted to administrators only (requires ROLE_ADMIN authority).
                    
                        **Query Parameters:**
                        - `page` (integer, optional): The page number to retrieve (0-based index). Default: `0`
                        - `size` (integer, optional): Number of records per page. Default: `10`
                    
                        **Response Structure:**
                        - `sellers` (array): List of seller objects awaiting approval
                        - `currentPage` (integer): Current page number
                        - `totalElements` (long): Total number of pending sellers
                        - `totalPage` (integer): Total number of pages
                    
                        **Use Cases:**
                        - Admin dashboard for managing seller approvals
                        - Reviewing seller registration requests
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved pending sellers",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/pending-seller")
    public ResponseEntity<?> findAllPendingSeller(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of records per page", example = "10")
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
                    
                        **Request Body (ApproveSellerRequest):**
                        - `sellerId` (Long)`: ID of the seller account to approve/reject
                        - `decision` (String)`: Decision status - typically "APPROVED" or "REJECTED"
                        - Additional fields may include reason, notes, etc.
                    
                        **Workflow:**
                        1. Admin submits approval/rejection data through this endpoint.
                        2. The system updates the seller's status.
                        3. A notification is constructed and timestamped (`sendAt`).
                        4. The notification is sent to the corresponding seller user through a socket event.
                    
                        **Response:**
                        - Success: Returns `ApproveSellerResponse` with updated seller info and notification details
                        - Error: Returns error message if seller not found or operation fails
                    
                        **Use cases:**
                        - Approving verified sellers after document validation.
                        - Rejecting invalid or incomplete seller registration requests.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seller approval/rejection processed successfully",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Seller not found")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/approve-seller")
    public ResponseEntity<RestResponse<?, ?>> handlePendingSeller(
            @Parameter(description = "Seller approval request containing sellerId and decision", required = true)
            @RequestBody ApproveSellerRequest request) throws JsonProcessingException {
        ApproveSellerResponse sellerNotification = sellerService.handlePendingSeller(request);
        sellerNotification.getNotification().setSendAt(DateUtils.getCurrentVietnamTime());
        socketController.sendUpgradeNotificationToUser(sellerNotification);
        return ResponseEntity.ok(responseMapper.toDto(true,
                "Approve request was be solved.",
                sellerNotification, null));
    }

    @Operation(
            summary = "Create a new admin account",
            description = """
                        Allows an existing administrator to create a new admin account in the system.
                        This endpoint accepts multipart/form-data with admin details and an avatar image.
                    
                        **Request Parameters (multipart/form-data):**
                        - `avatar_url` (file, required): Profile image file for the new admin (JPG, PNG, etc.)
                        - Form fields from `CreateAdminRequest`:
                          - `username` (String, required): Unique username for the admin
                          - `email` (String, required): Valid email address
                          - `password` (String, required): Password for the account
                          - `fullName` (String, optional): Full name of the admin
                          - `phoneNumber` (String, optional): Contact phone number
                          - `isSuperAdmin` (Boolean, optional): Whether this admin has super admin privileges
                    
                        **Response:**
                        - Success (200): Returns `AdminResponse` with created admin details
                        - Error (400/500): Returns error message if validation fails or creation fails
                    
                        **Use cases:**
                        - Registering additional admin users for system management.
                        - Managing multi-admin access in the platform.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin account created successfully",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data or validation failed"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @ApiResponse(responseCode = "500", description = "Internal server error during account creation")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("creating-admin")
    public ResponseEntity<?> handleCreatingAdmin(
            @Parameter(description = "Admin account details (username, email, password, etc.)", required = true)
            @Valid @ModelAttribute CreateAdminRequest request,
            @Parameter(description = "Profile image file (JPG, PNG, etc.)", required = true)
            @RequestPart(value = "avatar_url", required = true) MultipartFile avatarFile
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

    @Operation(
            summary = "Review Post Product List (Admin Only)",
            description = """
                        Retrieves a paginated list of post products that are pending verification or review by administrators.  
                        This API is restricted to users with the `ROLE_ADMIN` authority.
                    
                        **Query Parameters:**
                        - `page` (integer, optional): Page number (0-based index). Default: `0`
                        - `size` (integer, optional): Number of records per page. Default: `10`
                    
                        **Response Structure:**
                        - `data` (PostProductListResponse): Contains list of post products and pagination metadata
                          - `posts` (array): Array of post product objects
                          - `currentPage` (integer): Current page number
                          - `totalElements` (long): Total number of posts
                          - `totalPage` (integer): Total number of pages
                    
                        **Use cases:**
                        - Admins reviewing newly submitted product posts before approval.
                        - Moderators checking flagged or edited posts that require re-verification.
                        - Ensuring quality control and compliance of product listings before publication.
                    
                        **Security Notes:**
                        - Requires JWT authentication with `ROLE_ADMIN`.
                        - Unauthorized users (buyers/sellers) will be denied access.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved post products for review",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/review-post-seller-list")
    public ResponseEntity<RestResponse<PostProductListResponse, Object>> getAllPostProductForReview(
            @Parameter(description = "Number of records per page", example = "10")
            @RequestParam(name = "size", defaultValue = "10") int size,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page
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

    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER', 'ROLE_ADMIN')")
    @Operation(
            summary = "View post product details by ID",
            description = """
                        Retrieves detailed information for a specific post product based on its unique ID.  
                        Accessible to **Buyers**, **Sellers**, and **Admins** with appropriate privileges.
                    
                        **Path Parameters:**
                        - `postProductId` (Long, required): Unique identifier of the post product
                    
                        **Response Structure:**
                        - `data` (PostProductResponse): Contains complete product details including:
                          - Product information (title, description, price, brand, model, etc.)
                          - Seller information
                          - Product images
                          - Verification status
                          - Category and specifications
                    
                        **Use cases:**
                        - **Admin:** Reviewing pending or verified posts before approval or publication.
                        - **Seller:** Viewing or verifying their own product submission details.
                        - **Buyer:** Viewing detailed product information for browsing or purchasing decisions.
                    
                        **Security Notes:**
                        - Requires JWT authentication (`ROLE_BUYER`, `ROLE_SELLER`, or `ROLE_ADMIN`).
                        - Different roles may have access to different levels of detail based on internal authorization rules.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved post product details",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "Post product not found"),
            @ApiResponse(responseCode = "403", description = "Access denied - Authentication required")
    })
    @GetMapping("/{postProductId}/post-details")
    public ResponseEntity<RestResponse<PostProductResponse, Object>> viewPostProductDetail(
            @Parameter(description = "Unique identifier of the post product", required = true, example = "1")
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

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(
            summary = "Review and decide post product verification",
            description = """
                        Allows an admin or moderator to approve or reject a seller's post product after manual review.
                        This endpoint records the decision, updates the product's verification status, 
                        sends an email notification to the seller, and returns the updated product details.
                    
                        **Request Body (PostProductDecisionRequest):**
                        - `postProductId` (Long, required): ID of the post product to review
                        - `passed` (Boolean, required): `true` to approve, `false` to reject
                        - `reason` (String, optional): Reason for rejection (if applicable)
                        - `notes` (String, optional): Additional admin notes
                    
                        **Response:**
                        - Success (200): Returns updated `PostProductResponse` with new verification status
                        - Error (400/404): Returns error message if product not found or validation fails
                    
                        **Email Notification:**
                        - If approved: Sends congratulatory email to seller
                        - If rejected: Sends rejection notice with reason
                    
                        **Use cases:**
                        - Approving a verified product for listing.
                        - Rejecting a product submission with a reason or remark.
                        - Managing product moderation workflows.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post product decision processed successfully",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Post product not found")
    })
    @PostMapping("/review-post-product-decision")
    public ResponseEntity<RestResponse<PostProductResponse, Object>> reviewPostProductDecision(
            @Parameter(description = "Post product decision request containing postProductId and decision", required = true)
            @Valid @RequestBody PostProductDecisionRequest request) throws Exception {
        PostProduct result = postProductServiceImpl.checkPostProductVerification(request);

        MailRequest mailRequest = MailRequest.builder()
                .from("green.trade.platform.391@gmail.com")
                .to(result.getSeller().getBuyer().getEmail())
                .subject("Kết quả duyệt bài đăng sản phẩm")
                .build();
        log.info(">>> [Handle pending post]: Seller email: {}", result.getSeller().getBuyer().getEmail());

        // 3️⃣ Soạn nội dung email theo quyết định
        if (request.getPassed()) {
            mailRequest.setMessage("""
                    🎉 <strong>Chúc mừng bạn!</strong><br><br>
                    Bài đăng sản phẩm <strong>%s</strong> của bạn đã được phê duyệt thành công và hiện đang hiển thị trên hệ thống.<br><br>
                    Hãy đảm bảo rằng thông tin sản phẩm luôn chính xác và tuân thủ các <a href='https://green-trade-platform.com/policies' style='color:#4CAF50;font-weight:bold;'>chính sách bán hàng</a> của Green Trade.<br><br>
                    💚 Chúc bạn kinh doanh thuận lợi!
                    """.formatted(result.getTitle()));
        } else {
            mailRequest.setMessage("""
                    ⚠️ <strong>Rất tiếc!</strong><br><br>
                    Bài đăng sản phẩm <strong>%s</strong> của bạn chưa được phê duyệt.<br>
                    Nguyên nhân có thể do thông tin sản phẩm chưa đầy đủ hoặc vi phạm quy định của nền tảng.<br><br>
                    Vui lòng kiểm tra lại nội dung bài đăng và gửi yêu cầu phê duyệt lại sau khi điều chỉnh phù hợp.<br><br>
                    💚 Cảm ơn bạn đã hợp tác cùng Green Trade Platform!
                    """.formatted(result.getTitle()));
        }

        mailSender.sendBeautifulMail(mailRequest);
        PostProductResponse responseData = postProductMapper.toDto(result);
        RestResponse response = responseMapper.toDto(
                true,
                "POST HAS BEEN CHECKED",
                responseData,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @Operation(
            summary = "Block or unblock user account (buyer, seller, or admin)",
            description = """
                    This endpoint allows an **administrator** to block or unblock a user account based on its type and ID.  
                    Supported account types include **buyer**, **seller**, and **admin**.
                    
                    **Path Parameters:**
                    - `accountId` (Long, required): The ID of the account to block or unblock
                    - `accountType` (String, required): The type of the account. Valid values: `"buyer"`, `"seller"`, or `"admin"`
                    - `message` (String, required): A short explanation or note about the action (e.g., "Violation of policy")
                    - `activity` (String, required): Defines the action to perform. Valid values: `"block"` or `"unblock"`
                    
                    **Access Control:**
                    - Only users with `ROLE_ADMIN` can use this API
                    - For **admin** accounts: only a **super admin** can perform block or unblock operations
                    
                    **Response:**
                    - Success (200): Returns confirmation message (e.g., "BLOCK BUYER ACCOUNT SUCCESSFULLY")
                    - Error (400): Invalid account type or activity value
                    - Error (403): Insufficient permissions (e.g., non-super admin trying to block admin)
                    - Error (404): Account not found
                    
                    **Example Requests:**
                    - Block buyer: `POST /api/v1/admin/block-account/123/buyer/Violation%20of%20policy/block`
                    - Unblock seller: `POST /api/v1/admin/block-account/456/seller/Account%20restored/unblock`
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account blocked/unblocked successfully",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid account type or activity parameter"),
            @ApiResponse(responseCode = "403", description = "Access denied or insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PreAuthorize(("hasRole('ROLE_ADMIN')"))
    @PostMapping("/block-account/{accountId}/{accountType}/{message}/{activity}")
    public ResponseEntity<?> blockAccount(
            @Parameter(description = "ID of the account to block/unblock", required = true, example = "123")
            @PathVariable(name = "accountId") long id,
            @Parameter(description = "Account type: 'buyer', 'seller', or 'admin'", required = true, example = "buyer")
            @PathVariable(name = "accountType") String type,
            @Parameter(description = "Reason or message for the action", required = true, example = "Violation of policy")
            @PathVariable(name = "message") String message,
            @Parameter(description = "Action to perform: 'block' or 'unblock'", required = true, example = "block")
            @PathVariable(name = "activity") String activity
    ) {
        try {
            String successMessage;
            String actionText;

            // ✅ Xác định hành động (Block hoặc Unblock)
            if ("block".equalsIgnoreCase(activity)) {
                actionText = "BLOCK";
            } else if ("unblock".equalsIgnoreCase(activity)) {
                actionText = "UNBLOCK";
            } else {
                return ResponseEntity.badRequest().body(responseMapper.toDto(
                        false,
                        "INVALID ACTIVITY.",
                        null,
                        "Activity must be either 'block' or 'unblock'."
                ));
            }

            // ✅ Xử lý theo loại tài khoản
            if ("buyer".equalsIgnoreCase(type)) {
                buyerService.blockAccount(id, message, activity);
                successMessage = String.format("%s BUYER ACCOUNT SUCCESSFULLY.", actionText);
            } else if ("seller".equalsIgnoreCase(type)) {
                sellerService.blockAccount(id, message, activity);
                successMessage = String.format("%s SELLER ACCOUNT SUCCESSFULLY.", actionText);
            } else if ("admin".equalsIgnoreCase(type)) {
                Admin admin = adminService.getCurrentUser();
                if (!admin.isSuperAdmin()) {
                    throw new IllegalArgumentException("You do not have permission to block or unblock admin accounts.");
                }
                adminService.blockAccount(id, message, activity);
                successMessage = String.format("%s ADMIN ACCOUNT SUCCESSFULLY.", actionText);
            } else {
                return ResponseEntity.badRequest().body(responseMapper.toDto(
                        false,
                        "INVALID ACCOUNT TYPE.",
                        null,
                        "Type must be either 'buyer', 'seller', or 'admin'."
                ));
            }

            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    successMessage,
                    null,
                    null
            ));

        } catch (Exception e) {
            String actionText = activity.equalsIgnoreCase("unblock") ? "UNBLOCK" : "BLOCK";

            String errorMsg = switch (type.toLowerCase()) {
                case "buyer" -> String.format("%s BUYER ACCOUNT FAILED.", actionText);
                case "seller" -> String.format("%s SELLER ACCOUNT FAILED.", actionText);
                case "admin" -> String.format("%s ADMIN ACCOUNT FAILED.", actionText);
                default -> String.format("%s ACCOUNT FAILED.", actionText);
            };

            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    errorMsg,
                    null,
                    e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Retrieve paginated list of admin accounts",
            description = """
                    This endpoint allows an **administrator** to retrieve a paginated list of all admin accounts in the system.  
                    It supports pagination parameters (`page`, `size`) to efficiently navigate large datasets.
                    
                    **Query Parameters:**
                    - `page` (integer, optional): The page number to retrieve (0-based index). Default: `0`
                    - `size` (integer, optional): The number of records per page. Default: `10`
                    
                    **Response Structure:**
                    - `data` (Page<AdminResponse>): Paginated list containing:
                      - `content` (array): Array of admin account objects with details (id, username, email, isSuperAdmin, etc.)
                      - `totalElements` (long): Total number of admin accounts
                      - `totalPages` (integer): Total number of pages
                      - `number` (integer): Current page number
                      - `size` (integer): Page size
                    
                    **Access Control:** Only users with the role `ROLE_ADMIN` are authorized to access this endpoint.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved admin list",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<?> getAdminList(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Number of records per page", example = "10")
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        try {
            Page<Admin> admins = adminService.getAdminList(page, size);
            Page<AdminResponse> responses = admins.map(adminMapper::toDto);
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET LIST ADMIN ACCOUNT SUCCESSFULLY.",
                    responses, null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET LIST ADMIN ACCOUNT FAILED.",
                    null, e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Get admin profile by account ID",
            description = """
                    Retrieve the detailed profile information of an admin by their account ID.
                    Only users with the role ROLE_ADMIN can access this endpoint.
                    
                    **Path Parameters:**
                    - `accountId` (Long, required): Unique identifier of the admin account
                    
                    **Response Structure:**
                    - `data` (AdminResponse): Contains admin profile details:
                      - `id` (Long): Admin account ID
                      - `username` (String): Admin username
                      - `email` (String): Admin email address
                      - `fullName` (String): Full name of admin
                      - `phoneNumber` (String): Contact phone number
                      - `avatarUrl` (String): URL to profile image
                      - `isSuperAdmin` (Boolean): Whether admin has super admin privileges
                      - `status` (String): Account status (ACTIVE, INACTIVE, etc.)
                      - `createdAt` (LocalDateTime): Account creation timestamp
                    
                    **Example Request:**
                    GET /api/v1/admin/profile/5
                    Requires Authorization header with a valid JWT token.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved admin profile",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Admin account not found")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/profile/{accountId}")
    public ResponseEntity<?> getProfile(
            @Parameter(description = "Unique identifier of the admin account", required = true, example = "5")
            @PathVariable(name = "accountId") long id
    ) {
        try {
            Admin admin = adminService.getAdminProfile(id);
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET ADMIN PROFILE SUCCESSFULLY.",
                    adminMapper.toDto(admin), null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "GET ADMIN PROFILE FAILED.",
                    null, e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Get total new post products within a date range",
            description = """
                    This endpoint allows **administrators** to retrieve the total number of new post products 
                    created within a specific date range. Useful for analytics and reporting purposes.
                    
                    **Query Parameters:**
                    - `start_date` (LocalDate, required): Start date of the range in ISO format (`yyyy-MM-dd`)
                    - `end_date` (LocalDate, required): End date of the range in ISO format (`yyyy-MM-dd`)
                    
                    **Response:**
                    - `data` (Long): Total count of post products created between start_date and end_date (inclusive)
                    
                    **Example Request:**
                    GET /api/v1/admin/total-new-post?start_date=2025-01-01&end_date=2025-01-31
                    
                    **Note:**
                    - The result counts posts whose `createdAt` values fall within the given range (inclusive).
                    - Only users with role **ROLE_ADMIN** are authorized to access this endpoint.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved total new posts",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid date format or date range"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/total-new-post")
    public ResponseEntity<?> getTotalNewPostInMonth(
            @Parameter(description = "Start date in ISO format (yyyy-MM-dd)", required = true, example = "2025-01-01")
            @RequestParam("start_date") LocalDate startDate,
            @Parameter(description = "End date in ISO format (yyyy-MM-dd)", required = true, example = "2025-01-31")
            @RequestParam("end_date") LocalDate endDate
    ) {
        long totalPost = postProductService.getTotalNewPostInMonth(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
        return ResponseEntity.ok(responseMapper.toDto(
                true,
                "GET TOTAL NEW POST SUCCESSFULLY.",
                totalPost, null
        ));
    }

    @Operation(
            summary = "Get all pending post products for verification",
            description = """
                    This endpoint allows administrators to retrieve a paginated list of post products
                    that are pending verification. Accessible only by users with the ADMIN role.
                    
                    **Query Parameters:**
                    - `page` (integer, optional): Page number (0-based index). Default: `0`
                    - `size` (integer, optional): Number of records per page. Default: `10`
                    
                    **Response Structure:**
                    - `data` (Page<PostProductResponse>): Paginated list containing:
                      - `content` (array): Array of post product objects awaiting verification
                      - `totalElements` (long): Total number of pending posts
                      - `totalPages` (integer): Total number of pages
                      - `number` (integer): Current page number
                      - `size` (integer): Page size
                    
                    **Use Cases:**
                    - Admin dashboard for reviewing pending product submissions
                    - Quality control and content moderation
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved pending post products",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/pending-post")
    public ResponseEntity<?> getPendingVerifyPostProduct(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Number of records per page", example = "10")
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        try {
            Page<PostProduct> pendingPost = postProductService.getPendingVerifyPost(page, size);

            Page<PostProductResponse> response = pendingPost.map(postProductMapper::toDto);

            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET ALL PENDING VERIFY POST PRODUCT SUCCESSFULLY.",
                    response, null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET ALL PENDING VERIFY POST PRODUCT FAILED.",
                    null, e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Admin approves or rejects a product post",
            description = """
                    Allows admin to approve or reject a product post submitted by a seller.
                    The endpoint updates the post status and sends an email notification to the seller.
                    
                    **Path Parameters:**
                    - `postId` (Long, required): ID of the product post to approve/reject
                    - `decision` (VerifiedDecisionStatus, required): Decision status. Valid values: `APPROVED` or `REJECTED`
                    
                    **Response:**
                    - Success (200): Returns updated `PostProductResponse` with new verification status
                    - Error (400/404): Returns error message if post not found or validation fails
                    
                    **Email Notification:**
                    - If APPROVED: Sends congratulatory email to seller
                    - If REJECTED: Sends rejection notice with reason
                    
                    **Example Requests:**
                    - Approve: POST /api/v1/admin/approve-post/12/APPROVED
                    - Reject: POST /api/v1/admin/approve-post/12/REJECTED
                    
                    **Permissions:** ROLE_ADMIN required.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post approval/rejection processed successfully",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid decision status"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Post product not found")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/approve-post/{postId}/{decision}")
    public ResponseEntity<?> handlePendingPost(
            @Parameter(description = "ID of the product post", required = true, example = "12")
            @PathVariable(name = "postId") long id,
            @Parameter(description = "Decision status: APPROVED or REJECTED", required = true, example = "APPROVED")
            @PathVariable(name = "decision") VerifiedDecisionStatus decision) throws Exception {
        try {
            PostProduct postProduct = postProductServiceImpl.handlePendingPostProduct(id, decision);

            MailRequest mailRequest = MailRequest.builder()
                    .from("green.trade.platform.391@gmail.com")
                    .to(postProduct.getSeller().getBuyer().getEmail())
                    .subject("Kết quả duyệt bài đăng sản phẩm")
                    .build();
            log.info(">>> [Handle pending post]: Seller email: {}", postProduct.getSeller().getBuyer().getEmail());

            // 3️⃣ Soạn nội dung email theo quyết định
            if (decision == VerifiedDecisionStatus.APPROVED) {
                mailRequest.setMessage("""
                        🎉 <strong>Chúc mừng bạn!</strong><br><br>
                        Bài đăng sản phẩm <strong>%s</strong> của bạn đã được phê duyệt thành công và hiện đang hiển thị trên hệ thống.<br><br>
                        Hãy đảm bảo rằng thông tin sản phẩm luôn chính xác và tuân thủ các <a href='https://green-trade-platform.com/policies' style='color:#4CAF50;font-weight:bold;'>chính sách bán hàng</a> của Green Trade.<br><br>
                        💚 Chúc bạn kinh doanh thuận lợi!
                        """.formatted(postProduct.getTitle()));
            } else {
                mailRequest.setMessage("""
                        ⚠️ <strong>Rất tiếc!</strong><br><br>
                        Bài đăng sản phẩm <strong>%s</strong> của bạn chưa được phê duyệt.<br>
                        Nguyên nhân có thể do thông tin sản phẩm chưa đầy đủ hoặc vi phạm quy định của nền tảng.<br><br>
                        Vui lòng kiểm tra lại nội dung bài đăng và gửi yêu cầu phê duyệt lại sau khi điều chỉnh phù hợp.<br><br>
                        💚 Cảm ơn bạn đã hợp tác cùng Green Trade Platform!
                        """.formatted(postProduct.getTitle()));
            }

            mailSender.sendBeautifulMail(mailRequest);

            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "APPROVE POST SUCCESSFULLY.",
                    postProductMapper.toDto(postProduct), null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "APPROVE POST FAILED.",
                    null, e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Get all escrow system wallets",
            description = """
                    Retrieves a paginated list of system wallets with ESCROW_HOLD status. 
                    These are escrow records where money is being held before transfer to seller.
                    
                    **Query Parameters:**
                    - `page` (integer, optional): Page number (0-based index). Default: `0`
                    - `size` (integer, optional): Number of records per page. Default: `10`
                    
                    **Response Structure:**
                    - `data` (Page<SystemWalletResponse>): Paginated list containing:
                      - `content` (array): Array of system wallet objects with:
                        - `id` (Long): System wallet ID
                        - `order` (Order): Associated order information
                        - `balance` (BigDecimal): Amount held in escrow
                        - `status` (SystemWalletStatus): Current status (ESCROW_HOLD)
                        - `createdAt` (LocalDateTime): When escrow was created
                        - `endAt` (LocalDateTime): When escrow will be automatically resolved
                        - `buyerWalletId` (Long): Buyer's wallet ID
                        - `sellerWalletId` (Long): Seller's wallet ID
                      - `totalElements` (long): Total number of escrow records
                      - `totalPages` (integer): Total number of pages
                    
                    **Use Cases:**
                    - Admin monitoring escrow transactions
                    - Reviewing pending money transfers
                    - Managing escrow resolution timeline
                    
                    **Access Control:** Admin only (ROLE_ADMIN required).
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved escrow system wallets",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/system-wallets")
    public ResponseEntity<?> getSystemWallets(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Number of records per page", example = "10")
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        try {
            Page<SystemWallet> systemWallets = systemWalletService.getAllEscrowService(page, size);
            Page<SystemWalletResponse> responses = systemWallets.map(systemWalletMapper::toDto);
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "Get all escrow service (pending) successfully.",
                    responses, null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "Get all escrow service (pending) failed.",
                    null, e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Update system wallet endAt",
            description = """
                    Updates the `endAt` time of a system wallet (escrow record). 
                    This determines when the escrow will be automatically resolved and money transferred to seller.
                    
                    **Path Parameters:**
                    - `systemWalletId` (Long, required): Unique identifier of the system wallet to update
                    
                    **Request Body (UpdateSystemWalletEndAtRequest):**
                    - `endAt` (LocalDateTime, required): New end date/time in ISO format (`yyyy-MM-dd'T'HH:mm:ss`)
                    
                    **Validation Rules:**
                    - Only system wallets with status `ESCROW_HOLD` can be updated
                    - `endAt` must be after `createdAt`
                    - Only super admin can perform this operation
                    
                    **Response:**
                    - Success (200): Returns updated `SystemWalletResponse` with new `endAt` value
                    - Error (400): Invalid request (e.g., endAt before createdAt, wrong status)
                    - Error (403): Access denied (non-super admin)
                    - Error (404): System wallet not found
                    
                    **Example Request:**
                    PUT /api/v1/admin/system-wallets/1/end-at
                    {
                      "endAt": "2025-11-15T14:30:00"
                    }
                    
                    **Use Cases:**
                    - Adjusting escrow resolution timeline
                    - Extending or shortening escrow period for specific orders
                    - Manual intervention in escrow management
                    
                    **Access Control:** Super admin only (ROLE_ADMIN + isSuperAdmin = true).
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "System wallet endAt updated successfully",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request (wrong status, invalid date, etc.)"),
            @ApiResponse(responseCode = "403", description = "Access denied - Super admin required"),
            @ApiResponse(responseCode = "404", description = "System wallet not found")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/system-wallets/{systemWalletId}/end-at")
    public ResponseEntity<?> updateSystemWalletEndAt(
            @Parameter(description = "Unique identifier of the system wallet", required = true, example = "1")
            @PathVariable Long systemWalletId,
            @Parameter(description = "Request body containing new endAt datetime", required = true)
            @Valid @RequestBody UpdateSystemWalletEndAtRequest request
    ) {
        try {
            Admin admin = adminService.getCurrentUser();
            if (!admin.isSuperAdmin()) {
                throw new IllegalArgumentException("Only super admin can update system wallet endAt.");
            }

            // Sử dụng endAt từ request (đã được parse theo timezone của request)
            LocalDateTime endAt = request.getEndAt();

            SystemWallet updatedWallet = systemWalletService.updateEndAt(systemWalletId, endAt);
            SystemWalletResponse response = systemWalletMapper.toDto(updatedWallet);

            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "System wallet endAt updated successfully.",
                    response,
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "Failed to update system wallet endAt: " + e.getMessage(),
                    null,
                    e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Create a new subscription package",
            description = """
                    Allows a **super admin** to create a new subscription package in the system.
                    This endpoint is restricted to super administrators only.
                    
                    **Request Body (CreateSubscriptionPackageRequest):**
                    - `name` (String, required): Unique name of the subscription package
                    - `description` (String, required): Description of the package features
                    - `isActive` (Boolean, required): Whether the package is active and available
                    - `maxProduct` (Long, required): Maximum number of products allowed (must be positive)
                    - `maxImgPerPost` (Long, required): Maximum images per post (must be positive)
                    - `canSendVerifyRequest` (Boolean, required): Whether this package allows sellers to send verify requests for post products
                    - `prices` (List<PackagePriceRequest>, optional): List of package prices to create
                      - `price` (Double, required): Price amount (must be positive)
                      - `isActive` (Boolean, required): Whether this price option is active
                      - `durationByDay` (Long, required): Duration in days (must be positive)
                      - `currency` (String, required): Currency code (e.g., "VND")
                      - `discountPercent` (Double, required): Discount percentage (must be >= 0)
                    
                    **Response:**
                    - Success (200): Returns created `SubscriptionPackageResponse` with package details
                    - Error (400): Invalid request data or validation failed
                    - Error (403): Access denied (non-super admin)
                    - Error (409): Package name already exists
                    
                    **Use Cases:**
                    - Creating new subscription tiers with different features
                    - Configuring which packages allow verify request feature
                    - Managing subscription package offerings
                    
                    **Access Control:** Super admin only (ROLE_ADMIN + isSuperAdmin = true).
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscription package created successfully",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data or validation failed"),
            @ApiResponse(responseCode = "403", description = "Access denied - Super admin required"),
            @ApiResponse(responseCode = "409", description = "Package name already exists")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/subscription-packages")
    public ResponseEntity<?> createSubscriptionPackage(
            @Parameter(description = "Subscription package creation request", required = true)
            @Valid @RequestBody CreateSubscriptionPackageRequest request
    ) {
        try {
            Admin admin = adminService.getCurrentUser();
            if (!admin.isSuperAdmin()) {
                throw new IllegalArgumentException("Only super admin can create subscription packages.");
            }

            SubscriptionPackages createdPackage = subscriptionPackageService.createSubscriptionPackage(request);
            SubscriptionPackageResponse response = subscriptionPackageMapper.toResponse(createdPackage);

            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "Subscription package created successfully.",
                    response,
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "Failed to create subscription package: " + e.getMessage(),
                    null,
                    e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Update an existing subscription package",
            description = """
                    Allows a **super admin** to update an existing subscription package in the system.
                    This endpoint is restricted to super administrators only.
                    
                    **Path Parameters:**
                    - `packageId` (Long, required): Unique identifier of the subscription package to update
                    
                    **Request Body (UpdateSubscriptionPackageRequest):**
                    - `name` (String, required): Updated name of the subscription package
                    - `description` (String, required): Updated description of the package features
                    - `isActive` (Boolean, required): Whether the package is active and available
                    - `maxProduct` (Long, required): Maximum number of products allowed (must be positive)
                    - `maxImgPerPost` (Long, required): Maximum images per post (must be positive)
                    - `canSendVerifyRequest` (Boolean, required): Whether this package allows sellers to send verify requests for post products
                    - `prices` (List<PackagePriceRequest>, optional): List of package prices to update/create/delete
                      - `id` (Long, optional): Price ID if updating existing price, null if creating new
                      - `price` (Double, required): Price amount (must be positive)
                      - `isActive` (Boolean, required): Whether this price option is active
                      - `durationByDay` (Long, required): Duration in days (must be positive)
                      - `currency` (String, required): Currency code (e.g., "VND")
                      - `discountPercent` (Double, required): Discount percentage (must be >= 0)
                    
                    **Price Update Logic:**
                    - Prices with `id` in request → Update existing prices
                    - Prices without `id` in request → Create new prices
                    - Existing prices not in request → Soft delete (set deletedAt)
                    
                    **Response:**
                    - Success (200): Returns updated `SubscriptionPackageResponse` with package details
                    - Error (400): Invalid request data or validation failed
                    - Error (403): Access denied (non-super admin)
                    - Error (404): Subscription package not found
                    - Error (409): New package name conflicts with existing package
                    
                    **Use Cases:**
                    - Updating package features and limits
                    - Enabling/disabling verify request feature for specific packages
                    - Modifying package availability status
                    
                    **Access Control:** Super admin only (ROLE_ADMIN + isSuperAdmin = true).
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscription package updated successfully",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data or validation failed"),
            @ApiResponse(responseCode = "403", description = "Access denied - Super admin required"),
            @ApiResponse(responseCode = "404", description = "Subscription package not found"),
            @ApiResponse(responseCode = "409", description = "Package name already exists")
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/subscription-packages/{packageId}")
    public ResponseEntity<?> updateSubscriptionPackage(
            @Parameter(description = "Unique identifier of the subscription package", required = true, example = "1")
            @PathVariable Long packageId,
            @Parameter(description = "Subscription package update request", required = true)
            @Valid @RequestBody UpdateSubscriptionPackageRequest request
    ) {
        try {
            Admin admin = adminService.getCurrentUser();
            if (!admin.isSuperAdmin()) {
                throw new IllegalArgumentException("Only super admin can update subscription packages.");
            }

            SubscriptionPackages updatedPackage = subscriptionPackageService.updateSubscriptionPackage(packageId, request);
            SubscriptionPackageResponse response = subscriptionPackageMapper.toResponse(updatedPackage);

            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "Subscription package updated successfully.",
                    response,
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "Failed to update subscription package: " + e.getMessage(),
                    null,
                    e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Get all subscription packages (for admin)",
            description = """
                    This endpoint allows **super admin** to retrieve all subscription packages in the system,
                    including both active and inactive packages, with pagination support.
                    
                    **Query Parameters:**
                    - `page` (Integer, optional, default: 0): Page number (0-based)
                    - `size` (Integer, optional, default: 10): Number of items per page
                    
                    **Response includes:**
                    - Paginated list of all subscription packages (both active and inactive)
                    - For each package: Package details (name, description, features, prices)
                    - Pagination metadata (totalElements, totalPages, currentPage, pageSize)
                    
                    **Use Cases:**
                    - View all packages for management
                    - Edit or update packages
                    - See inactive packages that may need to be reactivated
                    
                    **Access Control:** Super admin only (ROLE_ADMIN + isSuperAdmin = true).
                    
                    **Example:**
                    ```
                    GET /api/v1/admin/subscription-packages?page=0&size=10
                    ```
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Subscription packages retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SubscriptionPackageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Super admin required"
            )
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/subscription-packages")
    public ResponseEntity<?> getAllSubscriptionPackages(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        try {
            Admin admin = adminService.getCurrentUser();
            if (!admin.isSuperAdmin()) {
                throw new IllegalArgumentException("Only super admin can view all subscription packages.");
            }

            Page<SubscriptionPackages> packages = subscriptionPackageService.getAllSubscriptionPackages(page, size);
            Page<SubscriptionPackageResponse> responses = packages.map(subscriptionPackageMapper::toResponse);

            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET ALL SUBSCRIPTION PACKAGES SUCCESSFULLY.",
                    responses,
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "GET ALL SUBSCRIPTION PACKAGES FAILED.",
                    null,
                    e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Get all subscription packages with statistics",
            description = """
                    This endpoint allows **super admin** to retrieve all subscription packages in the system
                    along with their statistics (total subscribers and total revenue).
                    
                    **Response includes:**
                    - List of all subscription packages (both active and inactive)
                    - For each package:
                      - Package details (name, description, features)
                      - Total number of sellers who purchased this package
                      - Total revenue generated from this package
                    
                    **Use Cases:**
                    - View all packages and their performance metrics
                    - Analyze which packages are most popular
                    - Monitor revenue by package
                    
                    **Access Control:** Super admin only (ROLE_ADMIN + isSuperAdmin = true).
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Subscription packages retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SubscriptionPackageListResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Super admin required"
            )
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/subscription-packages/statistics")
    public ResponseEntity<?> getAllSubscriptionPackagesWithStatistics() {
        try {
            Admin admin = adminService.getCurrentUser();
            if (!admin.isSuperAdmin()) {
                throw new IllegalArgumentException("Only super admin can view subscription package statistics.");
            }

            SubscriptionPackageListResponse response = subscriptionPackageService.getAllSubscriptionPackagesWithStatistics();

            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET ALL SUBSCRIPTION PACKAGES WITH STATISTICS SUCCESSFULLY.",
                    response,
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "GET ALL SUBSCRIPTION PACKAGES WITH STATISTICS FAILED.",
                    null,
                    e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Get detailed statistics for a specific subscription package",
            description = """
                    This endpoint allows **super admin** to retrieve detailed statistics for a specific subscription package.
                    
                    **Path Parameters:**
                    - `packageId` (Long, required): Unique identifier of the subscription package
                    
                    **Query Parameters:**
                    - `includeSubscribers` (Boolean, optional, default: false): Whether to include list of subscribers in response
                    
                    **Response includes:**
                    - Package details (name, description, features)
                    - Total number of sellers who purchased this package
                    - Total revenue generated from this package
                    - List of subscribers (if includeSubscribers = true):
                      - Seller information (ID, name, store name)
                      - Subscription details (price at purchase, start/end dates, status)
                    
                    **Use Cases:**
                    - View detailed package performance
                    - Analyze subscriber base for a specific package
                    - Monitor revenue for individual packages
                    
                    **Access Control:** Super admin only (ROLE_ADMIN + isSuperAdmin = true).
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Package statistics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SubscriptionPackageStatisticsResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Super admin required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Subscription package not found"
            )
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/subscription-packages/{packageId}/statistics")
    public ResponseEntity<?> getPackageStatistics(
            @Parameter(description = "Unique identifier of the subscription package", required = true, example = "1")
            @PathVariable Long packageId,
            @Parameter(description = "Whether to include list of subscribers", example = "false")
            @RequestParam(name = "includeSubscribers", defaultValue = "false") boolean includeSubscribers) {
        try {
            Admin admin = adminService.getCurrentUser();
            if (!admin.isSuperAdmin()) {
                throw new IllegalArgumentException("Only super admin can view subscription package statistics.");
            }

            SubscriptionPackageStatisticsResponse response = subscriptionPackageService.getPackageStatistics(packageId, includeSubscribers);

            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET PACKAGE STATISTICS SUCCESSFULLY.",
                    response,
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "GET PACKAGE STATISTICS FAILED.",
                    null,
                    e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Get list of sellers who purchased a specific subscription package",
            description = """
                    This endpoint allows **super admin** to retrieve a paginated list of sellers who have purchased
                    a specific subscription package.
                    
                    **Path Parameters:**
                    - `packageId` (Long, required): Unique identifier of the subscription package
                    
                    **Query Parameters:**
                    - `page` (Integer, optional, default: 0): Page number (0-based)
                    - `size` (Integer, optional, default: 10): Number of items per page
                    
                    **Response includes:**
                    - List of subscribers with:
                      - Seller information (ID, name, store name)
                      - Subscription details (ID, price at purchase, start/end dates, active status)
                    
                    **Use Cases:**
                    - View all sellers using a specific package
                    - Analyze subscriber demographics
                    - Contact sellers for package-related communications
                    
                    **Access Control:** Super admin only (ROLE_ADMIN + isSuperAdmin = true).
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Package subscribers retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SubscriberInfo.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Super admin required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Subscription package not found"
            )
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/subscription-packages/{packageId}/subscribers")
    public ResponseEntity<?> getPackageSubscribers(
            @Parameter(description = "Unique identifier of the subscription package", required = true, example = "1")
            @PathVariable Long packageId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(name = "size", defaultValue = "10") int size) {
        try {
            Admin admin = adminService.getCurrentUser();
            if (!admin.isSuperAdmin()) {
                throw new IllegalArgumentException("Only super admin can view subscription package subscribers.");
            }

            Page<SubscriberInfo> subscribersPage =
                    subscriptionPackageService.getPackageSubscribers(packageId, page, size);

            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET PACKAGE SUBSCRIBERS SUCCESSFULLY.",
                    subscribersPage,
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "GET PACKAGE SUBSCRIBERS FAILED.",
                    null,
                    e.getMessage()
            ));
        }
    }
}
