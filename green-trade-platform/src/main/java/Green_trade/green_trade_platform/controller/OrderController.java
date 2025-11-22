package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.enumerate.SystemWalletStatus;
import Green_trade.green_trade_platform.exception.OrderNotFound;
import Green_trade.green_trade_platform.mapper.*;
import Green_trade.green_trade_platform.model.*;
import Green_trade.green_trade_platform.repository.OrderRepository;
import Green_trade.green_trade_platform.request.CancelOrderRequest;
import Green_trade.green_trade_platform.request.ReviewRequest;
import Green_trade.green_trade_platform.request.UpdateReviewRequest;
import Green_trade.green_trade_platform.response.InvoiceResponse;
import Green_trade.green_trade_platform.response.*;
import Green_trade.green_trade_platform.service.implement.*;
import Green_trade.green_trade_platform.util.DateUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/order")
@Slf4j
@AllArgsConstructor
@Tag(name = "Order Management", description = "APIs for managing orders, reviews, and invoices")
public class OrderController {

    private final OrderServiceImpl orderService;
    private final ResponseMapper responseMapper;
    private final OrderListMapper orderListMapper;
    private final BuyerServiceImpl buyerService;
    private final GhnServiceImpl ghnService;
    private final OrderMapper orderMapper;
    private final ReviewServiceImpl reviewService;
    private final ReviewMapper reviewMapper;
    private final SystemWalletServiceImpl systemWalletService;
    private final ShippingPartnerMapper shippingPartnerMapper;
    private final PaymentMapper paymentMapper;
    private final PostProductMapper postProductMapper;
    private final BuyerMapper buyerMapper;
    private final OrderHistoryMapper orderHistoryMapper;
    private final OrderHistoryListMapper orderHistoryListMapper;
    private final OrderRepository orderRepository;
    private final InvoiceServiceImpl invoiceService;
    private final InvoiceMapper invoiceMapper;

    @Operation(
            summary = "Get order history of current user",
            description = """
                        Retrieves a paginated list of past orders belonging to the currently authenticated buyer.  
                        The system uses the access token to identify the buyer and fetches their order history, 
                        including details such as order ID, total amount, status, and order date.
                    
                        **Workflow:**
                        1. The frontend sends a request with pagination parameters (`page`, `size`).
                        2. The backend identifies the buyer from the JWT token.
                        3. The system retrieves a paginated list of the buyer’s orders, sorted by date (latest first).
                        4. Pagination metadata (current page, total pages, total elements) is included in the response.
                    
                        **Use cases:**
                        - Displaying a user’s order history in their profile dashboard.
                        - Fetching paginated order records for mobile or web apps.
                    
                        **Security Notes:**
                        - Requires authentication via JWT (`ROLE_BUYER`).
                        - Each user can only access their own order history.
                    """
    )
    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER')")
    @GetMapping("/history")
    public ResponseEntity<RestResponse<OrderHistoryListResponse, Object>> getOrdersHistoryOfCurrentUser(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        try {
            log.info(">>> [OrderController] came history");
            Buyer buyer = buyerService.getCurrentUser();
            log.info(">>> [OrderController] get current buyer successfully");
            Page<Order> orderPaging = orderService.getOrdersOfCurrentUserPaging(size, page, buyer);
            log.info(">>> [OrderController] get paging with order successfully");
            Map<String, Object> meta = Map.of(
                    "currentPage", orderPaging.getNumber(),
                    "totalElements", orderPaging.getTotalElements(),
                    "totalPage", orderPaging.getTotalPages()
            );
            log.info(">>> [OrderController] created meta data successfully");

            OrderHistoryListResponse orderHistoryListResponse = orderHistoryListMapper.toDto(orderPaging, meta);


            RestResponse<OrderHistoryListResponse, Object> response = responseMapper.toDto(
                    true,
                    "FETCH ORDER HISTORY SUCCESSFULLY",
                    orderHistoryListResponse,
                    null
            );
            log.info(">>> [OrderController] created response successfully");

            return ResponseEntity.status(HttpStatus.OK.value()).body(response);
        } catch (Exception e) {
            log.info(">>> Error at getOrdersHistoryOfCurrentUser: {}", e.getMessage());
            throw e;
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER')")
    @Operation(
            summary = "Cancel an order",
            description = """
                        Cancels an existing order for the currently authenticated user (buyer).  
                        This API updates the order status in the system and notifies the external GHN shipping service 
                        to cancel the corresponding shipment.
                    
                        **Workflow:**
                        1. The client sends a `POST` request with the order ID in the URL path.
                        2. The system validates that the order exists and belongs to the authenticated user.
                        3. The order status is updated to `CANCELED`.
                        4. The system calls the GHN shipping service API to cancel the shipping request.
                        5. The updated order information is returned in the response.
                    
                        **Use cases:**
                        - Buyers canceling an order before it is shipped.
                        - Sellers or system administrators canceling orders with failed payments or stock issues.
                        - Synchronizing order cancellations between internal system and GHN shipping API.
                    
                        **Security Notes:**
                        - Requires JWT authentication (either `ROLE_BUYER` or `ROLE_SELLER`).
                        - A user can only cancel orders they own.
                    """
    )
    @PostMapping("/cancel/{id}")
    public ResponseEntity<RestResponse<OrderResponse, Object>> cancelOrder(
            @PathVariable Long id,
            @RequestBody CancelOrderRequest request
    ) throws Exception {
        log.info(">>> [OrderController] came cancelOrder");
        Order canceledOrder = orderService.cancelOrder(id, request);
        log.info(">>> [OrderController] cancelOrder pass");
        systemWalletService.updateEscrowRecordStatus(canceledOrder.getSystemWallet(), SystemWalletStatus.IS_SOLVED);
        log.info(">>> [OrderController] update system wallet status successfully");
        ghnService.createCancelOrderShippingServiceResponseToDto(canceledOrder.getOrderCode(), canceledOrder.getPostProduct().getSeller().getGhnShopId());
        log.info(">>> [OrderController] cancel order ghn successfully");
        OrderResponse responseData = orderMapper.toDto(canceledOrder);
        log.info(">>> [OrderController] created responseData successfully");
        RestResponse<OrderResponse, Object> response = responseMapper.toDto(
                true,
                "CANCELED ORDER SUCCESSFULLY",
                responseData,
                null
        );
        log.info(">>> [OrderController] created response successfully");
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @Operation(
            summary = "Create a product review with optional images",
            description = """
                    This endpoint allows customers to create a review for an electrical product they have purchased.
                    
                    The request should include:
                    - **Review details** (order ID, rating, feedback text) as a JSON object named `request`.
                    - **Optional product images** (photos of the product or proof of use) as `pictures`.
                    
                    The API automatically checks the feedback text for inappropriate or offensive language (Vietnamese supported).
                    Uploaded images will be stored on Cloudinary and associated with the review record.
                    
                    **Content type:** multipart/form-data  
                    **Authentication:** Required if the platform uses user accounts.
                    """
    )
    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER')")
    @PostMapping(
            value = "/review",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> createReview(@ModelAttribute ReviewRequest request,
                                          @RequestPart(name = "pictures", required = false) List<MultipartFile> reviewImages) {
        log.info(">>> [Order Controller] Create Review: Started.");
        log.info(">>> [Order Controller] Request: {}.", request);
        try {
            Review savedReview = reviewService.createReview(request, reviewImages);
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "MAKE REVIEW SUCCESSFULLY.",
                    reviewMapper.toDto(savedReview), null));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "MAKE REVIEW FAILED.",
                    null, e.getMessage()));
        }
    }

    @Operation(
            summary = "Get all reviews by order ID",
            description = """
                    This endpoint allows an authenticated user (buyer or seller) to retrieve all reviews 
                    associated with a specific order.
                    
                    - The `orderId` parameter must correspond to an existing order.
                    - Each review may include its rating, feedback text, and attached review images.
                    """
    )
    @GetMapping("/get-review/{orderId}")
    public ResponseEntity<?> getReviewByOrderId(@PathVariable(name = "orderId") long id) {
        try {
            Review reviews = reviewService.getReviewsByOrderId(id);
            ReviewResponse response = reviewMapper.toDto(reviews);

            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET REVIEWS BY ORDER SUCCESSFULLY.",
                    response,
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "GET REVIEWS BY ORDER FAILED.",
                    null,
                    e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Update a product review",
            description = """
                    This endpoint allows customers to update their existing review for a product they have purchased.
                    
                    The request should include:
                    - **Review details** (rating, feedback text) as a JSON object named `request`.
                    - **Optional new product images** (photos of the product or proof of use) as `pictures`.
                    
                    **Important Notes:**
                    - Only the buyer who created the review can update it.
                    - If new images are provided, old images will be replaced.
                    - The API automatically checks the feedback text for inappropriate or offensive language (Vietnamese supported).
                    - Rating must be between 0 and 5.
                    
                    **Content type:** multipart/form-data  
                    **Authentication:** Required - must be the buyer who created the review.
                    """
    )
    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER')")
    @PutMapping(
            value = "/review/{reviewId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> updateReview(
            @PathVariable(name = "reviewId") Long reviewId,
            @ModelAttribute UpdateReviewRequest request,
            @RequestPart(name = "pictures", required = false) List<MultipartFile> newImages) {
        log.info(">>> [Order Controller] Update Review: Started. ReviewId: {}", reviewId);
        log.info(">>> [Order Controller] Request: {}.", request);
        try {
            // Kiểm tra quyền: chỉ buyer của order mới được update review
            Review existingReview = reviewService.getReviewById(reviewId);
            Buyer currentBuyer = buyerService.getCurrentUser();

            if (existingReview.getOrder() == null || existingReview.getOrder().getBuyer() == null) {
                return ResponseEntity.ok(responseMapper.toDto(
                        false,
                        "UPDATE REVIEW FAILED.",
                        null,
                        "Order information not found."
                ));
            }

            if (!existingReview.getOrder().getBuyer().getBuyerId().equals(currentBuyer.getBuyerId())) {
                return ResponseEntity.ok(responseMapper.toDto(
                        false,
                        "UPDATE REVIEW FAILED.",
                        null,
                        "You are not authorized to update this review. Only the buyer who created the review can update it."
                ));
            }

            // Kiểm tra system wallet: nếu đã kết thúc (IS_SOLVED) thì không cho update review
            Order order = existingReview.getOrder();
            if (order.getSystemWallet() != null) {
                SystemWallet systemWallet = order.getSystemWallet();
                SystemWalletStatus status = systemWallet.getStatus();

                // Kiểm tra nếu system wallet đã kết thúc (IS_SOLVED)
                if (status == SystemWalletStatus.IS_SOLVED) {
                    return ResponseEntity.ok(responseMapper.toDto(
                            false,
                            "UPDATE REVIEW FAILED.",
                            null,
                            "Cannot update review. The system wallet for this order has already been completed."
                    ));
                }

                // Kiểm tra nếu endAt đã qua (tiền đã được giải phóng hoặc sắp được giải phóng)
                if (systemWallet.getEndAt() != null) {
                    LocalDateTime currentTime = DateUtils.getCurrentVietnamTime();
                    if (systemWallet.getEndAt().isBefore(currentTime) ||
                            systemWallet.getEndAt().isEqual(currentTime)) {
                        return ResponseEntity.ok(responseMapper.toDto(
                                false,
                                "UPDATE REVIEW FAILED.",
                                null,
                                "Cannot update review. The system wallet for this order has already ended."
                        ));
                    }
                }
            }

            Review updatedReview = reviewService.updateReview(reviewId, request, newImages);
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "UPDATE REVIEW SUCCESSFULLY.",
                    reviewMapper.toDto(updatedReview),
                    null
            ));
        } catch (Exception e) {
            log.error(">>> [Order Controller] Update Review Failed: {}", e.getMessage());
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "UPDATE REVIEW FAILED.",
                    null,
                    e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Get payment information by order ID",
            description = """
                    This endpoint retrieves the payment details associated with a specific order. 
                    It returns the payment ID, description, and gateway name linked to that order.
                    If the order or transaction does not exist, an error message will be returned.
                    """
    )
    @GetMapping("/payment/{orderId}")
    public ResponseEntity<?> getPayment(@PathVariable(name = "orderId") long id) {
        log.info(">>> [Order Controller] Get payment: Started.");
        try {
            Transaction transaction = orderService.getTransactionByOrderId(id);
            log.info(">>> [Order Controller] Get transaction: {}", transaction);
            Payment payment = transaction.getPayment();
            log.info(">>> [Order Controller] Get payment: {}", payment);
            Map<String, Object> data = new HashMap<>();
            data.put("id", payment.getId());
            data.put("description", payment.getDescription());
            data.put("gatewayName", payment.getGatewayName());
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET ORDER PAYMENT SUCCESSFULLY.",
                    data, null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "GET ORDER PAYMENT FAILED.",
                    null, e.getMessage()
            ));
        }

    }

    @Operation(
            summary = "Get detailed order information by ID",
            description = """
                    This endpoint retrieves detailed information about a specific order using its ID.
                    The response includes details about:
                    - The order itself
                    - The product associated with the order
                    - The buyer who placed the order
                    - The payment information
                    - The shipping partner handling the order
                    If the order ID does not exist, an `OrderNotFound` exception will be thrown.
                    """
    )
    @GetMapping("/{orderId}")
    public ResponseEntity<RestResponse<Map<String, Object>, Object>> getOrderDetailByOrderId(@PathVariable Long orderId) {
        Order foundOrder = orderService.getOrderById(orderId);
        if (foundOrder == null) {
            throw new OrderNotFound();
        }

        OrderResponse orderResponse = orderMapper.toDto(foundOrder);
        PaymentResponse paymentResponse = paymentMapper.toDto(foundOrder.getTransactions().getLast().getPayment());
        ShippingPartnerResponse shippingPartnerResponse = shippingPartnerMapper.toDto(foundOrder.getShippingPartner());
        PostProductResponse productResponse = postProductMapper.toDto(foundOrder.getPostProduct());
        BuyerResponse buyerResponse = buyerMapper.toDto(foundOrder.getBuyer());


        Map<String, Object> responseData = new HashMap<>();
        responseData.put("order", orderResponse);
        responseData.put("product", productResponse);
        responseData.put("buyer", buyerResponse);
        responseData.put("payment", paymentResponse);
        responseData.put("shippingPartner", shippingPartnerResponse);
        
        // Add invoice API if order has invoice
        if (foundOrder.getInvoice() != null) {
            responseData.put("invoiceApi", "/api/v1/order/" + orderId + "/invoice");
        }

        RestResponse<Map<String, Object>, Object> response = responseMapper.toDto(
                true,
                "FETCH ORDER DETAIL SUCCESSFULLY",
                responseData,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @Operation(
            summary = "Get all orders of current seller",
            description = """
                    Retrieve all orders associated with the current logged-in seller.
                    The result is paginated using 'page' and 'size' query parameters.
                    
                    Requirements:
                    - User must have ROLE_SELLER
                    - Authorization header with a valid JWT token is required
                    
                    Example:
                    GET /api/orders?page=0&size=10
                    """
    )
    @PreAuthorize("hasRole('ROLE_SELLER')")
    @GetMapping("")
    public ResponseEntity<?> getAllOrders(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        try {
            Seller seller = buyerService.getCurrentUser().getSeller();
            Page<Order> orders = orderService.getAllOrders(page, size, seller);
            Page<OrderResponse> orderResponses = orders.map(orderMapper::toDto);
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET ALL ORDER SUCCESSFULLY.",
                    orderResponses, null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET ALL ORDER FAILED.",
                    null, e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Get invoice by order ID",
            description = """
                    Retrieves the invoice associated with a specific order. If the invoice does not exist,
                    a new invoice will be automatically created and generated.
                    
                    **Workflow:**
                    1. System retrieves order by orderId
                    2. Checks if invoice exists for the order
                    3. If invoice doesn't exist:
                       - Creates a new invoice instance
                       - Generates invoice number
                    4. Returns invoice details
                    
                    **Response Includes:**
                    - Invoice ID and invoice number
                    - Order information
                    - Invoice date and status
                    - Total amount and tax information
                    
                    **Use Cases:**
                    - Viewing order invoice for buyers
                    - Downloading invoice for accounting
                    - Sellers viewing order invoices
                    
                    **Security:**
                    - Public endpoint - No authentication required
                    - Invoice is automatically created if missing
                    """,
            tags = {"Order Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Invoice retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "FETCH INVOICE SUCCESSFULLY",
                                              "data": {
                                                "id": 1,
                                                "invoiceNumber": "INV-2024-001",
                                                "orderId": 123,
                                                "invoiceDate": "2024-01-15T10:30:00",
                                                "totalAmount": 5030000.00,
                                                "taxAmount": 0.00,
                                                "status": "GENERATED"
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": false,
                                              "message": "Order not found",
                                              "data": null,
                                              "error": "Order with ID 123 does not exist"
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/{orderId}/invoice")
    public ResponseEntity<RestResponse<InvoiceResponse, Object>> getInvoiceByOrderId(
            @Parameter(
                    description = "The ID of the order to get invoice for",
                    required = true,
                    example = "123"
            )
            @PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        Invoice invoice = order.getInvoice();
        if (invoice == null) {
            invoice = invoiceService.createInvoiceInstance(order, "", 0.0);
            invoiceService.generateInvoice(invoice.getId());
        }
        InvoiceResponse invoiceResponse = invoiceMapper.toDto(invoice);
        RestResponse<InvoiceResponse, Object> response = responseMapper.toDto(
                true,
                "FETCH INVOICE SUCCESSFULLY",
                invoiceResponse,
                null
        );
        return ResponseEntity.ok(response);
    }
}
