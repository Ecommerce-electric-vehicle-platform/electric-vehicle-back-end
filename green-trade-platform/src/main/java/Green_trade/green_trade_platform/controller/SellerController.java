package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.exception.PostProductNotFound;
import Green_trade.green_trade_platform.mapper.OrderMapper;
import Green_trade.green_trade_platform.mapper.PostProductMapper;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.mapper.ReviewMapper;
import Green_trade.green_trade_platform.mapper.SellerMapper;
import Green_trade.green_trade_platform.model.Order;
import Green_trade.green_trade_platform.model.PostProduct;
import Green_trade.green_trade_platform.model.Seller;
import Green_trade.green_trade_platform.model.Subscription;
import Green_trade.green_trade_platform.request.UpdatePostProductRequest;
import Green_trade.green_trade_platform.request.UploadPostProductRequest;
import Green_trade.green_trade_platform.request.VerifiedPostProductRequest;
import Green_trade.green_trade_platform.response.*;
import Green_trade.green_trade_platform.model.Review;
import Green_trade.green_trade_platform.service.ReviewService;
import Green_trade.green_trade_platform.service.implement.OrderServiceImpl;
import Green_trade.green_trade_platform.mapper.FollowingMapper;
import Green_trade.green_trade_platform.service.implement.FollowingServiceImpl;
import Green_trade.green_trade_platform.service.implement.PostProductServiceImpl;
import Green_trade.green_trade_platform.service.implement.SellerServiceImpl;
import Green_trade.green_trade_platform.service.implement.SubscriptionPackageServiceImpl;
import Green_trade.green_trade_platform.model.Following;
import Green_trade.green_trade_platform.response.FollowerResponse;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/seller")
@Slf4j
@AllArgsConstructor
@Tag(name = "Seller Management", description = "APIs for seller operations including product management, order handling, revenue tracking, and subscription verification")
public class SellerController {

    private final ResponseMapper responseMapper;
    private final SellerServiceImpl sellerService;
    private final SellerMapper sellerMapper;
    private final PostProductServiceImpl postProductService;
    private final PostProductMapper postProductMapper;
    private final OrderServiceImpl orderService;
    private final OrderMapper orderMapper;
    private final SubscriptionPackageServiceImpl subscriptionPackageService;
    private final ReviewService reviewService;
    private final ReviewMapper reviewMapper;
    private final FollowingServiceImpl followingService;
    private final FollowingMapper followingMapper;

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @Operation(
            summary = "Verify Service Package Validity",
            description = """
                    This endpoint allows a **seller** to verify whether their current service package is still valid.
                    <br><br>
                    Workflow:
                    <ul>
                        <li>Checks the service subscription associated with the seller's username.</li>
                        <li>Returns whether the package is valid and the expiry date.</li>
                    </ul>
                    """
    )
    @PostMapping("/{username}/check-service-package-validity")
    public ResponseEntity<RestResponse<SubscriptionResponse, Object>> checkServicePackageValidity(@PathVariable String username) throws Exception {
        SubscriptionResponse result = sellerService.checkServicePackageValidity(username);
        RestResponse<SubscriptionResponse, Object> response = responseMapper.toDto(
                true,
                "Service Package is valid",
                result,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @Operation(
            summary = "Upload a product post for selling",
            description = """
                    This endpoint allows a **seller** to upload a new post for a product they want to sell.
                    <br><br>
                    The request consists of:
                    <ul>
                        <li>Product details (title, brand, model, price, etc.) in form-data.</li>
                        <li>One or more product images uploaded as multipart files.</li>
                    </ul>
                    The response returns the created post details after saving it successfully.
                    """
    )
    @PostMapping("/post-products")
    public ResponseEntity<RestResponse<PostProductResponse, Object>> uploadPostProduct(
            @ModelAttribute UploadPostProductRequest request,
            @RequestPart("pictures") List<MultipartFile> files
    ) throws Exception {
        log.info(">>> Passed came uploadPostProduct");
        log.info(">>> Passed mapped files data: {}", files);
        Seller seller = sellerService.getCurrentUser();
        request.setSellerId(seller.getSellerId());

        Subscription subscription = subscriptionPackageService.updateRemainPost(seller);

        PostProduct newPostProduct = postProductService.createNewPostProduct(request, files);

        PostProductResponse responseData = postProductMapper.toDto(newPostProduct);

        RestResponse<PostProductResponse, Object> response = responseMapper.toDto(
                true,
                "UPLOADED POST SUCCESSFULLY",
                responseData,
                null
        );

        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @Operation(
            summary = "Upload post product pictures to Cloudinary",
            description = """
                        Allows a seller to upload one or more pictures for a specific post product.  
                        The uploaded images are stored on Cloudinary, and the post product record is updated 
                        with the image URLs.
                    
                        **Workflow:**
                        1. The seller provides the post product ID as a path parameter.
                        2. Multiple images are sent as multipart files in the `pictures` request part.
                        3. The system uploads each file to Cloudinary and associates the image URLs with the post product.
                        4. The endpoint returns the updated post product details including all image URLs.
                    
                        **Use cases:**
                        - Sellers adding images for a newly created product listing.
                        - Updating existing listings with better or additional pictures.
                        - Synchronizing image uploads with Cloudinary storage.
                    
                        **Security Notes:**
                        - Requires a valid JWT token with `ROLE_SELLER` authority.
                        - Only the owner of the product can upload or modify its images.
                        - File validation (e.g., image size and type) should be enforced on both client and server.
                    """
    )
    @PreAuthorize("hasRole('ROLE_SELLER')")
    @PostMapping("/upload-pictures-cloudinary/{id}")
    public ResponseEntity<RestResponse<PostProductResponse, Object>> uploadPostProduct(
            @PathVariable Long id,
            @RequestPart("pictures") List<MultipartFile> files
    ) throws Exception {
        log.info(">>> Passed came uploadPostProduct");
        log.info(">>> Passed mapped files data: {}", files);
        PostProduct newPostProduct = postProductService.uploadPostProductPicture(id, files);
        PostProductResponse responseData = postProductMapper.toDto(newPostProduct);
        RestResponse<PostProductResponse, Object> response = responseMapper.toDto(
                true,
                "UPLOADED POST PICTURES SUCCESSFULLY",
                responseData,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @Operation(
            summary = "Request verification for a post product",
            description = """
                        Allows a seller to request verification for a specific post product.  
                        This process typically ensures that the post product meets platform standards 
                        (e.g., authenticity, completeness, compliance) before being made public or promoted.
                    
                        **Workflow:**
                        1. The seller sends a verification request for one of their post products.
                        2. The system validates ownership and product eligibility.
                        3. The post product status changes to `PENDING_VERIFICATION`.
                        4. The platform’s review team will then approve or reject the verification request.
                    
                        **Use cases:**
                        - Sellers submitting products for manual review or moderation before publishing.
                        - Quality control and fraud prevention workflows.
                        - Enabling verified products to gain higher trust and visibility on the platform.
                    
                        **Security Notes:**
                        - Requires authentication via JWT token with `ROLE_SELLER`.
                        - Sellers can only request verification for products they own.
                    """
    )
    @PostMapping("/verified-post-product-request")
    public ResponseEntity<RestResponse<PostProductResponse, Object>> postProductVerifiedRequest(@Valid @RequestBody VerifiedPostProductRequest request) throws Exception {
        PostProduct result = postProductService.postProductVerifiedRequest(request);
        PostProductResponse responseData = postProductMapper.toDto(result);
        RestResponse<PostProductResponse, Object> response = responseMapper.toDto(
                true,
                "VERIFIED POST REQUEST SENT",
                responseData,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @Operation(
            summary = "Get seller profile",
            description = """
                        Retrieves the profile information of the currently authenticated seller.  
                        The frontend only needs to send the JWT access token in the `Authorization` header;  
                        the system identifies the seller automatically from the token.
                    
                        **Workflow:**
                        1. The client sends a `GET /seller/profile` request with an Authorization header:  
                           `Authorization: Bearer <access_token>`
                        2. The system verifies the access token and identifies the seller.
                        3. The seller’s profile details are fetched and returned in the response.
                    
                        **Use cases:**
                        - Displaying seller account details in their dashboard or settings.
                        - Allowing sellers to view their store and verification status.
                        - Returning only the seller’s own profile based on token authentication.
                    
                        **Security Notes:**
                        - Requires a valid JWT token with `ROLE_SELLER`.
                        - Each seller can only access their own profile.
                    """
    )
    @PreAuthorize("hasRole('ROLE_SELLER')")
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            Seller seller = sellerService.getCurrentUser();
            return ResponseEntity.ok(responseMapper.toDto(true,
                    "Get seller profile successfully.",
                    sellerMapper.toDto(seller),
                    null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(responseMapper.toDto(false,
                    "Error occured during get seller profile,",
                    null, e.getMessage()));
        }
    }

    @Operation(
            summary = "Get all product posts created by the authenticated seller",
            description = """
                    This endpoint retrieves a list of all products (posts) that were created by the currently authenticated seller account.
                    
                    The API identifies the seller based on the authentication token (JWT or session context) included in the request header.
                    It returns a list of product posts that belong exclusively to that seller.
                    
                    **Usage notes:**
                    - Only users with a **Seller** role can access this endpoint.
                    - Each returned post contains product information such as title, price, quantity, description, and creation date.
                    - Supports pagination and filtering (if applicable).
                    
                    **Authentication:** Required (Bearer Token)
                    """
    )
    @GetMapping("/seller-post")
    @PreAuthorize("hasAnyRole('ROLE_SELLER', 'ROLE_BUYER')")
    public ResponseEntity<?> getAllPostBySeller(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        log.info(">>> [Post Product Controller] Started get all post by seller.");

        Seller seller = sellerService.getCurrentUser();
        log.info(">>> [Post Product Controller] Seller: {}", seller.getSellerName());

        Page<PostProduct> posts = postProductService.getAllPostBySeller(seller, page, size);
        Page<PostProductResponse> responsePage = postProductMapper.toDtoPage(posts);

        return ResponseEntity.ok(responseMapper.toDto(
                true,
                "GET POST PRODUCT BY SELLER SUCCESSFULLY.",
                responsePage, null
        ));
    }

    @Operation(
            summary = "Get total number of followers for the authenticated seller",
            description = """
                    This endpoint returns the total count of buyers who are currently following the authenticated seller.
                    
                    The API identifies the seller based on the authentication token (JWT or session context) included in the request header.
                    It returns a simple integer representing the total number of active followers.
                    
                    **Usage notes:**
                    - Only users with a **Seller** role can access this endpoint.
                    - Returns the count of active followers only (excludes unfollowed buyers).
                    - Response is a simple integer value.
                    
                    **Response:**
                    - Returns an integer representing the total number of followers
                    
                    **Authentication:** Required (Bearer Token)
                    """,
            tags = {"Seller Management"}
    )
    @GetMapping("/followers")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<?> getFollowersCount() {
        try {
            log.info(">>> [Seller Controller] Get followers count");
            
            Seller seller = sellerService.getCurrentUser();
            log.info(">>> [Seller Controller] Seller: {} (ID: {})", seller.getSellerName(), seller.getSellerId());
            
            Long followersCount = followingService.countFollowersBySeller(seller.getSellerId());
            
            log.info(">>> [Seller Controller] Get followers count successfully: {} followers", followersCount);
            
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET FOLLOWERS COUNT SUCCESSFULLY.",
                    followersCount.intValue(), // Trả về số nguyên
                    null
            ));
        } catch (Exception e) {
            log.error(">>> [Seller Controller] Get followers count failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).body(responseMapper.toDto(
                    false,
                    "GET FOLLOWERS COUNT FAILED: " + e.getMessage(),
                    null,
                    e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Retrieve all pending orders for the current seller",
            description = """
                    This endpoint returns a paginated list of orders that are currently in **PENDING** status
                    and belong to the authenticated seller.
                    
                    - It requires the user to be logged in as a seller.
                    - Each order in the response is mapped to the `OrderResponse` DTO.
                    - Pagination is supported using the `page` and `size` query parameters.
                    
                    **Use case:**  
                    Sellers can use this endpoint to track customer orders that have been placed
                    but not yet processed, shipped, or completed.
                    """
    )
    @PreAuthorize("hasRole('ROLE_SELLER')")
    @GetMapping("/pending-orders")
    public ResponseEntity<?> getAllPendingOrders(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        try {
            Seller seller = sellerService.getCurrentUser();
            Page<Order> pendingOrder = orderService.getPendingOrders(seller, page, size);
            Page<OrderResponse> response = pendingOrder.map(orderMapper::toDto);
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET ALL PENDING ORDER SUCCESSFULLY.",
                    response, null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "GET ALL PENDING ORDER FAILED.",
                    null, e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Retrieve all delivering orders for the current seller",
            description = """
                    This endpoint returns a paginated list of orders that are currently in **DELIVERING** status
                    and belong to the authenticated seller.
                    
                    - It requires the user to be logged in as a seller.
                    - Each order in the response is mapped to the `OrderResponse` DTO.
                    - Pagination is supported using the `page` and `size` query parameters.
                    - Results are sorted by creation date (newest first).
                    
                    **Use case:**  
                    Sellers can use this endpoint to track orders that are currently being delivered to customers.
                    """
    )
    @PreAuthorize("hasRole('ROLE_SELLER')")
    @GetMapping("/delivering-orders")
    public ResponseEntity<?> getAllDeliveringOrders(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        try {
            Seller seller = sellerService.getCurrentUser();
            Page<Order> deliveringOrders = orderService.getDeliveringOrders(seller, page, size);
            Page<OrderResponse> response = deliveringOrders.map(orderMapper::toDto);
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET ALL DELIVERING ORDER SUCCESSFULLY.",
                    response, null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "GET ALL DELIVERING ORDER FAILED.",
                    null, e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Retrieve all completed orders for the current seller",
            description = """
                    This endpoint returns a paginated list of orders that are currently in **COMPLETED** status
                    and belong to the authenticated seller.
                    
                    - It requires the user to be logged in as a seller.
                    - Each order in the response is mapped to the `OrderResponse` DTO.
                    - Pagination is supported using the `page` and `size` query parameters.
                    - Results are sorted by creation date (newest first).
                    
                    **Use case:**  
                    Sellers can use this endpoint to view their order history of successfully completed orders.
                    """
    )
    @PreAuthorize("hasRole('ROLE_SELLER')")
    @GetMapping("/completed-orders")
    public ResponseEntity<?> getAllCompletedOrders(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        try {
            Seller seller = sellerService.getCurrentUser();
            Page<Order> completedOrders = orderService.getCompletedOrders(seller, page, size);
            Page<OrderResponse> response = completedOrders.map(orderMapper::toDto);
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET ALL COMPLETED ORDER SUCCESSFULLY.",
                    response, null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "GET ALL COMPLETED ORDER FAILED.",
                    null, e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Seller accepts (verifies) a pending order",
            description = """
                    This endpoint allows the **authenticated seller** to verify or accept a customer's pending order.
                    
                    - The seller must own the product (PostProduct) associated with the order.
                    - Once verified, the order status will change from **PENDING** to **VERIFIED**.
                    - This action signifies that the seller agrees to fulfill the order and proceed to the shipping or payment phase.
                    - If the order is not found, does not belong to the seller, or has already been processed (not pending), an appropriate error will be returned.
                    
                    **Use case:**  
                    Sellers use this endpoint to approve customer orders before shipment or payment confirmation.
                    """
    )
    @PostMapping("/verify-order/{orderId}")
    public ResponseEntity<?> verifyPendingOrder(@PathVariable(name = "orderId") long id) {
        try {
            Order order = orderService.verifyOrder(id);
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "VERIFY ORDER SUCCESSFULLY",
                    orderMapper.toDto(order), null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "VERIFY ORDER FAILED",
                    null, e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Retrieve paginated list of sellers",
            description = """
                    This endpoint allows a **seller** to retrieve a paginated list of all sellers registered on the platform. 
                    It supports pagination parameters (`page`, `size`) to manage large data sets efficiently.
                    
                    **Access Control:** Only users with the role `ROLE_SELLER` are authorized to access this endpoint.
                    
                    **Response:**
                    - On success: Returns a paginated list of `SellerResponse` objects containing seller details.
                    - On failure: Returns an error response with a failure message and exception details.
                    """
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<?> getSellerList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        try {
            Page<Seller> sellers = sellerService.getSellerList(page, size);
            Page<SellerResponse> response = sellers.map(sellerMapper::toDto2);
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET SELLER LIST SUCCESSFULLY.",
                    response, null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "GET SELLER LIST FAILED.",
                    null, e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Update an existing post product",
            description = """
                    Allows a seller to update details of an existing product post.
                    Only users with the role **ROLE_SELLER** can perform this operation.
                    The post cannot be modified if the product has already been sold.
                    """,
            security = {@SecurityRequirement(name = "bearerAuth")},
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Post product updated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RestResponse.class)
                            )
                    )
            }
    )
    @PreAuthorize("hasRole('ROLE_SELLER')")
    @PutMapping(
            value = "/{postId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<RestResponse<PostProductResponse, Object>> updatePostProduct(
            @PathVariable Long postId,
            @Valid @ModelAttribute UpdatePostProductRequest request,
            @RequestPart(value = "pictures", required = false) List<MultipartFile> files
    ) throws Exception {
        try {
            PostProduct foundPostProduct = postProductService.findPostProductById(postId);
            if (foundPostProduct == null) {
                throw new PostProductNotFound();
            }

            if (foundPostProduct.isSold()) {
                throw new Exception("Sold product's post cannot be changed the content");
            }

            PostProduct updatedPostProduct = postProductService.updatePostProduct(foundPostProduct, request, files);

            PostProductResponse responseData = postProductMapper.toDto(updatedPostProduct);

            RestResponse<PostProductResponse, Object> response = responseMapper.toDto(
                    true,
                    "UPDATED POST PRODUCT SUCCESSFULLY",
                    responseData,
                    null
            );

            return ResponseEntity.status(HttpStatus.OK.value()).body(response);
        } catch (Exception e) {
            log.info(">>> [PostProductController] error at updatePostProduct: {}", e.getMessage());
            throw e;
        }
    }

    @Operation(
            summary = "Hide or unhide a post product by ID",
            description = """
                        Hides (deactivates) a specific post product from the platform by setting its `active` status to `false`.  
                        The product remains stored in the database but will no longer be visible to buyers or appear in public listings.
                    
                        **Workflow:**
                        1. The client sends a request with the `postId` of the product to hide.
                        2. The system verifies ownership or admin privileges.
                        3. The product’s `active` flag is updated to `false`.
                        4. A confirmation response is returned with the updated product details.
                    
                        **Use cases:**
                        - **Seller:** Temporarily hides a product that is out of stock or under maintenance.
                        - **Admin:** Moderates or disables posts violating policies.
                        - **Buyer (optional):** Typically not allowed; only for viewing hidden-state results if permitted.
                    
                        **Security Notes:**
                        - Requires authentication via JWT.
                        - Accessible to roles: `ROLE_SELLER`, `ROLE_ADMIN`.
                        - The request is **idempotent** — hiding an already hidden product returns the same result.
                    """
    )
    @PreAuthorize("hasRole('ROLE_SELLER')")
    @PostMapping("/hide/{postId}")
    public ResponseEntity<?> hidePostProduct(@PathVariable(name = "postId") Long id,
                                             @RequestParam(name = "is_hide") boolean isHide) {
        String hide = isHide ? "HIDE" : "FALSE";
        log.info(">>> [Hide post product controller] Started.");
        try {
            PostProduct temp = postProductService.hidePostProduct(id, isHide);
            log.info(">>> [Hide post product controller] temp info {}.", temp.getId());
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    hide + " PRODUCT SUCCESSFULLY.",
                    postProductMapper.toDto(temp), null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    hide + " PRODUCT SUCCESSFULLY.",
                    null, e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Get total number of sellers",
            description = "This endpoint retrieves the total number of registered sellers in the system. " +
                    "Accessible only by users with the ADMIN role."
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/total-sellers")
    public ResponseEntity<?> getTotalSellers() {
        return ResponseEntity.ok(responseMapper.toDto(
                true,
                "GET TOTAL SELLERS SUCCESSFULLY.",
                sellerService.getTotalSellers(), null
        ));
    }

    @Operation(
            summary = "Get total pending orders for current seller",
            description = """
                    Returns the total number of orders that are currently in the **PENDING** state 
                    for the authenticated seller.
                    
                    **Authorization:** Requires `ROLE_SELLER`.
                    
                    **Example:**
                    ```
                    GET /api/seller/total-pending-order
                    ```
                    **Response:**
                    ```json
                    {
                        "success": true,
                        "message": "GET ALL PENDING ORDER SUCCESSFULLY.",
                        "data": 5
                    }
                    ```
                    """
    )
    @PreAuthorize("hasRole('ROLE_SELLER')")
    @GetMapping("/total-pending-order")
    public ResponseEntity<?> getTotalPendingOrder() {
        try {
            Seller seller = sellerService.getCurrentUser();

            int count = sellerService.getTotalPendingOrder(seller);

            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET ALL PENDING ORDER SUCCESSFULLY.",
                    count, null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "GET ALL PENDING ORDER FAILED.",
                    null, e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Get total active posts for current seller",
            description = """
                    Retrieves the total number of **active posts** (approved and visible products) 
                    created by the authenticated seller.
                    
                    **Authorization:** Requires `ROLE_SELLER`.
                    
                    **Example:**
                    ```
                    GET /api/seller/active-post
                    ```
                    **Response:**
                    ```json
                    {
                        "success": true,
                        "message": "GET ALL ACTIVE POST SUCCESSFULLY.",
                        "data": 8
                    }
                    ```
                    """
    )
    @PreAuthorize("hasRole('ROLE_SELLER')")
    @GetMapping("/active-post")
    public ResponseEntity<?> getAllActivePost() {
        try {
            Seller seller = sellerService.getCurrentUser();
            int count = postProductService.countAllActivePost(seller);
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET ALL ACTIVE POST SUCCESSFULLY.",
                    count, null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "GET ALL ACTIVE POST FAILED.",
                    null, e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Get all orders for current seller with pagination",
            description = """
                    Returns a paginated list of **all orders** associated with the authenticated seller, 
                    regardless of their status (Pending, Completed, Canceled, etc.).
                    
                    The response includes:
                    - List of orders (OrderResponse) with pagination
                    - Total count of all orders (in Page metadata: totalElements)
                    
                    **Query Parameters:**
                    - `page` (Integer, optional, default: 0): Page number (0-based)
                    - `size` (Integer, optional, default: 10): Number of items per page
                    
                    **Authorization:** Requires `ROLE_SELLER`.
                    
                    **Example:**
                    ```
                    GET /api/v1/seller/total-order?page=0&size=10
                    ```
                    **Response:**
                    ```json
                    {
                        "success": true,
                        "message": "GET ALL ORDERS SUCCESSFULLY.",
                        "data": {
                            "orders": [...],
                            "totalOrders": 27,
                            "currentPage": 0,
                            "totalPages": 3,
                            "pageSize": 10
                        }
                    }
                    ```
                    """
    )
    @PreAuthorize("hasRole('ROLE_SELLER')")
    @GetMapping("/total-order")
    public ResponseEntity<?> getAllOrder(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        try {
            Seller seller = sellerService.getCurrentUser();
            Page<Order> orders = orderService.getAllOrders(page, size, seller);
            Page<OrderResponse> orderResponses = orders.map(orderMapper::toDto);

            SellerOrdersResponse response = SellerOrdersResponse.builder()
                    .orders(orderResponses.getContent())
                    .totalOrders(orderResponses.getTotalElements())
                    .currentPage(orderResponses.getNumber())
                    .totalPages(orderResponses.getTotalPages())
                    .pageSize(orderResponses.getSize())
                    .build();

            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET ALL ORDERS SUCCESSFULLY.",
                    response, null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "GET ALL ORDERS FAILED.",
                    null, e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Get total revenue for current seller",
            description = """
                    Retrieves the total revenue generated by the authenticated seller from all completed orders.
                    
                    **Workflow:**
                    1. System identifies the authenticated seller from JWT token
                    2. Calculates total revenue from all completed orders
                    3. Returns total revenue amount in VND
                    
                    **Response:**
                    - Returns total revenue as BigDecimal (in VND)
                    
                    **Use Cases:**
                    - Seller dashboard showing total earnings
                    - Financial reporting for sellers
                    - Revenue tracking and analytics
                    
                    **Security:**
                    - Requires authentication (ROLE_SELLER)
                    - Only returns revenue for authenticated seller's own orders
                    """,
            tags = {"Seller Revenue Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Total revenue retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "GET TOTAL REVENUE.",
                                              "data": 15000000.00,
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Failed to get total revenue",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Failed Response",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "GET TOTAL FAILED.",
                                              "data": null,
                                              "error": "Error message"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required"
            )
    })
    @PreAuthorize("hasRole('ROLE_SELLER')")
    @GetMapping("/total-revenue")
    public ResponseEntity<?> getTotalRevenue() {
        try {
            Seller seller = sellerService.getCurrentUser();
            BigDecimal total = orderService.getTotalRevenue(seller);
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET TOTAL REVENUE.",
                    total, null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "GET TOTAL FAILED.",
                    null, e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Get all reviews for current seller's orders",
            description = """
                    This endpoint returns a paginated list of all reviews/ratings that customers have given
                    for orders belonging to the authenticated seller.
                    
                    - It requires the user to be logged in as a seller.
                    - Each review includes rating, feedback text, and associated review images.
                    - Results are sorted by review ID (newest first).
                    - Pagination is supported using the `page` and `size` query parameters.
                    
                    **Use case:**  
                    Sellers can use this endpoint to view customer feedback and ratings to improve their
                    product quality and customer service.
                    
                    **Example:**
                    ```
                    GET /api/v1/seller/reviews?page=0&size=10
                    ```
                    """
    )
    @PreAuthorize("hasRole('ROLE_SELLER')")
    @GetMapping("/reviews")
    public ResponseEntity<?> getAllReviews(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        try {
            Seller seller = sellerService.getCurrentUser();
            Page<Review> reviews = reviewService.getReviewsBySeller(seller, page, size);
            Page<ReviewResponse> response = reviews.map(reviewMapper::toDto);
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET ALL REVIEWS SUCCESSFULLY.",
                    response, null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "GET ALL REVIEWS FAILED.",
                    null, e.getMessage()
            ));
        }
    }
}
