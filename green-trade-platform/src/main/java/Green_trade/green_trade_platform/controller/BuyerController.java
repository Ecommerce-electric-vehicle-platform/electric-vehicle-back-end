package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.enumerate.OrderStatus;
import Green_trade.green_trade_platform.enumerate.TransactionStatus;
import Green_trade.green_trade_platform.enumerate.WishListPriority;
import Green_trade.green_trade_platform.exception.OrderNotFound;
import Green_trade.green_trade_platform.exception.PaymentMethodNotSupportedException;
import Green_trade.green_trade_platform.exception.PostProductNotFound;
import Green_trade.green_trade_platform.exception.ProfileException;
import Green_trade.green_trade_platform.exception.SelfPurchaseNotAllowedException;
import Green_trade.green_trade_platform.mapper.*;
import Green_trade.green_trade_platform.model.*;
import Green_trade.green_trade_platform.repository.*;
import Green_trade.green_trade_platform.request.PlaceOrderRequest;
import Green_trade.green_trade_platform.request.ProfileRequest;
import Green_trade.green_trade_platform.request.UpdateBuyerProfileRequest;
import Green_trade.green_trade_platform.request.WishListRequest;
import Green_trade.green_trade_platform.response.*;
import Green_trade.green_trade_platform.service.implement.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/buyer")
@Slf4j
public class BuyerController {
    private final BuyerServiceImpl buyerService;
    private final ResponseMapper responseMapper;
    private final BuyerMapper buyerMapper;
    private final WalletMapper walletMapper;
    private final PaymentRepository paymentRepository;
    private final TransactionServiceImpl transactionService;
    private final OrderMapper orderMapper;
    private final GhnServiceImpl ghnService;
    private final BuyerRepository buyerRepository;
    private final PostProductRepository postProductRepository;
    private final TransactionRepository transactionRepository;
    private final OrderRepository orderRepository;
    private final OrderServiceImpl orderService;
    private final PostProductServiceImpl postProductService;
    private final PaymentServiceImpl paymentService;
    private final SystemWalletServiceImpl systemWalletService;
    private final WalletServiceImpl walletService;
    private final WishListMapper wishListMapper;
    private final WishListingServiceImpl wishListingService;
    private final InvoiceServiceImpl invoiceService;

    public BuyerController(
            BuyerServiceImpl buyerService,
            ResponseMapper responseMapper,
            BuyerMapper buyerMapper,
            WalletMapper walletMapper,
            PaymentRepository paymentRepository,
            TransactionServiceImpl transactionService,
            OrderMapper orderMapper,
            GhnServiceImpl ghnService,
            BuyerRepository buyerRepository,
            PostProductRepository postProductRepository,
            TransactionRepository transactionRepository,
            OrderRepository orderRepository,
            OrderServiceImpl orderService,
            PostProductServiceImpl postProductService,
            PaymentServiceImpl paymentService,
            SystemWalletServiceImpl systemWalletService,
            WalletServiceImpl walletService,
            WishListMapper wishListMapper,
            WishListingServiceImpl wishListingService,
            InvoiceServiceImpl invoiceService) {
        this.buyerService = buyerService;
        this.responseMapper = responseMapper;
        this.buyerMapper = buyerMapper;
        this.walletMapper = walletMapper;
        this.paymentRepository = paymentRepository;
        this.transactionService = transactionService;
        this.orderMapper = orderMapper;
        this.ghnService = ghnService;
        this.buyerRepository = buyerRepository;
        this.postProductRepository = postProductRepository;
        this.transactionRepository = transactionRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.postProductService = postProductService;
        this.paymentService = paymentService;
        this.systemWalletService = systemWalletService;
        this.walletService = walletService;
        this.wishListMapper = wishListMapper;
        this.wishListingService = wishListingService;
        this.invoiceService = invoiceService;
    }

    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER')")
    @Operation(
            summary = "Upload buyer profile",
            description = """
                        Allows a buyer to upload or update their profile information including full name,
                        shipping address, contact details, and avatar image.  
                        This endpoint accepts multipart form data containing both profile fields and an image file.
                    
                        **Workflow:**
                        1. The buyer submits profile data (name, address, etc.) and an avatar image via multipart form.
                        2. The system uploads the avatar file, updates the buyer's profile in the database, 
                           and returns the updated profile data.
                        3. Only authenticated buyers (ROLE_BUYER) can access this endpoint.
                    
                        **Use cases:**
                        - Buyers updating their account profile for the first time.
                        - Allowing users to change their avatar or update shipping address information.
                    
                        **Security Notes:**
                        - Requires valid JWT token with `ROLE_BUYER` authority.
                        - The uploaded image must comply with allowed size and format restrictions.
                    """
    )
    @PostMapping(
            value = "/upload-profile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadBuyerProfile(@Parameter(description = "profile request for buyer")
                                                @Valid @ModelAttribute ProfileRequest profileRequest,
                                                @Parameter(description = "avatar of buyer")
                                                @RequestPart(value = "avatar_url", required = true) MultipartFile avatarFile) throws IOException {
        Map<String, Object> body = buyerService.uploadBuyerProfile(profileRequest, avatarFile);
        Buyer tempProfile = (Buyer) body.get("profile");
        return ResponseEntity.ok(responseMapper.toDto(
                true,
                "UPLOAD PROFILE SUCCESS.",
                buyerMapper.toDto(tempProfile),
                null));
    }

    @Operation(
            summary = "Update Buyer Profile",
            description = """
                        Allows a buyer to update their existing profile information, including full name, 
                        contact details, shipping address, and optionally their avatar image.  
                        This endpoint accepts multipart/form-data requests where both text fields and a file may be included.
                    
                        **Workflow:**
                        1. The buyer submits updated profile details and, optionally, a new avatar image.
                        2. The system updates the corresponding fields in the buyer’s profile.
                        3. If a new avatar is provided, the image is uploaded and replaces the previous one.
                        4. The response returns the updated buyer profile information.
                    
                        **Use cases:**
                        - Buyers updating their personal information such as name, phone number, or address.
                        - Changing or removing an avatar profile picture.
                    
                        **Security Notes:**
                        - Requires authentication via JWT token (ROLE_BUYER).
                        - Only the owner of the account can update their own profile.
                    """
    )
    @PutMapping(
            value = "/update-profile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER')")
    public ResponseEntity<RestResponse<BuyerResponse, Object>> updateProfile(
            @Valid @ModelAttribute UpdateBuyerProfileRequest updateProfileRequest,
            @RequestPart(value = "avatar_url", required = false) MultipartFile avatarFile
    ) throws Exception {
        log.info(">>> Passed came updateProfile API");
        log.info(">>> updateProfileRequest: {}", updateProfileRequest);
        log.info(">>> avatarFile: {}", avatarFile);

        Buyer buyer = buyerService.updateProfile(updateProfileRequest, avatarFile);
        BuyerResponse responseData = buyerMapper.toDto(buyer);

        return ResponseEntity.status(HttpStatus.OK.value()).body(
                responseMapper.toDto(
                        true,
                        "UPDATED PROFILE SUCCESSFULLY",
                        responseData,
                        null
                )
        );
    }

    @Operation(
            summary = "Get buyer profile",
            description = """
                        Retrieves the profile information of the currently authenticated buyer.  
                        The client must include a valid JWT access token in the `Authorization` header.  
                        The system will decode the token, identify the buyer, and return their corresponding profile details.
                    
                        **Workflow:**
                        1. The client sends a `GET /profile` request with an Authorization header:  
                           `Authorization: Bearer <access_token>`
                        2. The system validates the access token.
                        3. The system identifies the buyer associated with the token.
                        4. The buyer’s profile is fetched and returned as a response.
                    
                        **Use cases:**
                        - Retrieving current logged-in buyer’s profile for display in their dashboard.
                        - Ensuring front-end applications can show user-specific information without manually passing user IDs.
                    
                        **Security Notes:**
                        - Requires a valid access token (`ROLE_BUYER`).
                        - Access is limited to the authenticated buyer’s own profile.
                    """
    )
    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER')")
    @GetMapping("/profile")
    public ResponseEntity<RestResponse<Object, Object>> getProfile() {
        try {
            Buyer buyer = buyerService.getCurrentUser();
            return ResponseEntity.ok(responseMapper.toDto(true,
                    "Get user profile successfully.",
                    buyerMapper.toDto(buyer),
                    null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(responseMapper.toDto(false,
                    "Error occur during get user profile.",
                    null, e));
        }
    }

    @Operation(
            summary = "Get user wallet.",
            description = "Front-end put access token in the header request. " +
                    "Back-end will give user's wallet information."
    )
    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER')")
    @GetMapping("/wallet")
    public ResponseEntity<RestResponse<Object, Object>> getWallet() {
        try {
            Wallet wallet = buyerService.getWallet();
            return ResponseEntity.ok(responseMapper.toDto(true,
                    "Get wallet's information successfully.",
                    walletMapper.toDto(wallet), null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(responseMapper.toDto(false,
                    "Get wallet information failed.",
                    null, e));
        }
    }

    @Operation(
            summary = "Place a new order",
            description = """
                    This endpoint allows a buyer to place a new order in the Green Trade platform.
                    
                    ## Workflow:
                    1. **Validation Phase:**
                       - Validates payment method exists and is supported
                       - Verifies buyer account exists and is active
                       - Checks product exists and is available (not sold)
                       - Prevents self-purchase (buyer cannot buy their own product)
                    
                    2. **Shipping Fee Calculation:**
                       - For **COD payment**: Calculates shipping fee including product value
                       - For **Online/Wallet payment**: Calculates shipping fee with zero value (payment handled separately)
                       - Uses GHN API to get accurate shipping costs
                    
                    3. **Order Creation:**
                       - Creates new order record in database
                       - Sets order status based on payment method
                       - Links order to buyer, product, and shipping partner
                    
                    4. **Payment Processing:**
                       - **COD Flow**: Creates transaction with PENDING status, creates GHN shipping order
                       - **Wallet Flow**: Deducts from buyer wallet, creates PAID transaction, creates GHN shipping order
                       - Creates escrow record to hold funds until order completion
                    
                    5. **Post-Order Actions:**
                       - Generates invoice for the order
                       - Updates product status to SOLD
                       - Returns order details with shipping code
                    
                    ## Payment Methods:
                    - **COD (Cash on Delivery)**: Payment made when receiving goods
                    - **Wallet/Online Payment**: Payment deducted immediately from buyer's wallet
                    
                    ## Security:
                    - Requires authentication (ROLE_BUYER or ROLE_SELLER)
                    - Validates all input data
                    - Prevents unauthorized purchases
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Order placement request with buyer and shipping information",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PlaceOrderRequest.class),
                            examples = @ExampleObject(
                                    name = "Example Request",
                                    value = """
                                            {
                                              "postProductId": 123,
                                              "username": "buyer123",
                                              "fullName": "Nguyễn Văn A",
                                              "street": "123 Đường ABC",
                                              "wardName": "Phường 1",
                                              "districtName": "Quận 1",
                                              "provinceName": "TP. Hồ Chí Minh",
                                              "phoneNumber": "0912345678",
                                              "shippingPartnerId": 1,
                                              "paymentId": 1
                                            }
                                            """
                            )
                    )
            ),
            tags = {"Order Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order placed successfully",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "PLACE ORDERED SUCCESS",
                                              "data": {
                                                "id": 456,
                                                "orderCode": "GHN123456789",
                                                "shippingAddress": "123 Đường ABC, Phường 1, Quận 1, TP. Hồ Chí Minh",
                                                "phoneNumber": "0912345678",
                                                "price": 5000000.00,
                                                "shippingFee": 30000.00,
                                                "status": "PENDING",
                                                "createdAt": "2024-01-15T10:30:00",
                                                "updatedAt": "2024-01-15T10:30:00",
                                                "canceledAt": null,
                                                "cancelOrderReasonResponse": null
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Validation errors or invalid input",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Invalid Product ID",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "message": "Product ID must be a positive number",
                                                      "data": null,
                                                      "error": {
                                                        "field": "postProductId",
                                                        "message": "Product ID must be a positive number"
                                                      }
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Phone Number",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "message": "Phone number must be valid (starts with 0 or +84 and has 10–11 digits)",
                                                      "data": null,
                                                      "error": {
                                                        "field": "phoneNumber",
                                                        "message": "Phone number must be valid"
                                                      }
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Unauthorized",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "Unauthorized - Please login",
                                              "data": null,
                                              "error": "Authentication required"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Resource not found",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Product Not Found",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "message": "Post product not found",
                                                      "data": null,
                                                      "error": "Product with ID 123 does not exist"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Buyer Not Found",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "message": "Buyer with Username: buyer123 is not existed",
                                                      "data": null,
                                                      "error": "Profile not found"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Payment Method Not Found",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "message": "Payment method not found",
                                                      "data": null,
                                                      "error": "Payment method with ID 1 does not exist"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - Business rule violation",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Self Purchase Not Allowed",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "message": "You cannot purchase your own product",
                                                      "data": null,
                                                      "error": "Self purchase is not allowed"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Product Already Sold",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "message": "Product is already sold",
                                                      "data": null,
                                                      "error": "Product with ID 123 has been sold"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Unprocessable Entity - Payment method not supported",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Payment Method Not Supported",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "Payment method not supported",
                                              "data": null,
                                              "error": "The selected payment method is not available"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Server Error",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "Internal server error occurred",
                                              "data": null,
                                              "error": "An unexpected error occurred while processing your order"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER')")
    @PostMapping("/place-order")
    public ResponseEntity<RestResponse<OrderResponse, Object>> placeOrder(
            @Valid 
            @RequestBody 
            @Parameter(
                    description = "Order placement request containing product, buyer, shipping, and payment information",
                    required = true
            )
            PlaceOrderRequest request
    ) throws Exception {
        Order newOrder = null;
        RestResponse<OrderResponse, Object> response = null;
        OrderResponse responseData = null;
        String shippingFee = "0";
        try {
            log.info(">>> [START] placeOrder");

            log.info(">>> Fetch payment");
            Payment payment = paymentService.findPaymentMethodById(request.getPaymentId());
            if (payment == null) {
                throw new PaymentMethodNotSupportedException();
            }

            log.info(">>> Fetch buyer");
            Buyer buyer = buyerService.findBuyerByUsername(request.getUsername());
            if (buyer == null) {
                throw new ProfileException("Buyer with Username: " + request.getUsername() + "is not existed");
            }

            log.info(">>> Fetch post product");
            PostProduct postProduct = postProductService.findPostProductById(request.getPostProductId());
            if (postProduct == null) {
                throw new PostProductNotFound();
            }

            if (buyer.getBuyerId() == postProduct.getSeller().getBuyer().getBuyerId()) {
                throw new SelfPurchaseNotAllowedException();
            }

            log.info(">>> Calculate shipping fee");
            if (payment.getGatewayName().equals("COD")) {
                log.info(">>> Calculate shipping fee COD");
                shippingFee = ghnService.getShippingFeeDto(buyer, postProduct.getSeller(), postProduct, postProduct.getPrice().intValue()).get("total");
            } else {
                log.info(">>> Calculate shipping fee Online Payment");
                shippingFee = ghnService.getShippingFeeDto(buyer, postProduct.getSeller(), postProduct, 0).get("total");
            }

            log.info(">>> Place new order");
            newOrder = buyerService.placeOrder(request, shippingFee);

            if ("COD".equalsIgnoreCase(payment.getGatewayName())) {
                //quy trình transaction
                //tạo transaction
                log.info(">>> COD payment flow");
                Transaction transaction = transactionService.checkoutCODPayment(
                        request.getUsername(),
                        request.getPostProductId(),
                        request.getPaymentId(),
                        newOrder
                );
                //lấy danh sách các transaction liên quan đến đơn hàng
                List<Transaction> transactions = transactionService.getTransactionsOfOrder(newOrder);
                log.info(">>> Passed get transactions");

                //lưu danh sách các transaction liên quan đến đơn hàng vào đơn hàng
                newOrder = orderService.updateOrderTransactions(newOrder, transactions);
                log.info(">>> Passed update transactions");
                //Kết thúc transaction

                //gọi api của ghn để tạo đơn hàng vận chuyển
                Map<String, String> createOrderShippingResponse = ghnService.createOrderShippingResponseToDto(
                        newOrder, transactionRepository.findAllByOrder(newOrder).getLast().getPayment()
                );

                //lấy mã vận đơn gán vào order
                String orderShippingCode = createOrderShippingResponse.get("orderCode");
                log.info(">>> Passed get orderShippingCode: {}", orderShippingCode);
                //cập nhật mã vận đơn vào order
                newOrder = orderService.updateOrderCode(orderShippingCode, newOrder);

                //lấy tổng phí dịch vụ để cập nhật Shipping Fee
                String totalServiceFee = createOrderShippingResponse.get("totalFee");
                log.info(">>> Passed get totalServiceFee: {}", totalServiceFee);
                //cập nhật tổng phí dịch vụ vào đơn hàng
                orderService.updateShippingFee(newOrder, totalServiceFee);
                log.info(">>> Passed set Order Code");

                //cập nhật tổng tiền của transaction
                transactionService.updateAmount(transactions.getLast(), newOrder.getPrice().add(newOrder.getShippingFee()));

                //tạo escrow cho đơn hàng
                SystemWallet systemWallet = systemWalletService.createEscrowRecordForCOD(newOrder, totalServiceFee);
                newOrder = orderService.updateSystemWallet(systemWallet, newOrder);
            } else {
                log.info(">>> Wallet payment flow");
                //tạo đơn hàng giả để lấy phí dịch vụ thật
                Map<String, String> createOrderShippingResponseDemo = ghnService.createOrderShippingResponseToDto(
                        newOrder, payment
                );
                //lưu lại orderCode để xoá đơn hàng giả
                String orderShippingCodeDemo = createOrderShippingResponseDemo.get("orderCode");
                log.info(">>> Passed get orderShippingCodeDemo: {}", orderShippingCodeDemo);

                newOrder = orderService.updateOrderCode(orderShippingCodeDemo, newOrder);
                log.info(">>> Passed set Order Code Demo");

                //lưu phí dịch vụ thật
                String totalServiceFeeDemo = createOrderShippingResponseDemo.get("totalFee");
                log.info(">>> Passed get totalServiceFeeDemo: {}", totalServiceFeeDemo);
                orderService.updateShippingFee(newOrder, totalServiceFeeDemo);

                ghnService.createCancelOrderShippingServiceResponseToDto(newOrder.getOrderCode(), newOrder.getPostProduct().getSeller().getGhnShopId());
                //vào flow chính sau khi đã có phí dịch vụ thật

                Transaction transaction = transactionService.checkoutWalletPayment(
                        request.getUsername(),
                        request.getPostProductId(),
                        request.getPaymentId(),
                        newOrder
                );

                //lấy danh sách transactions
                List<Transaction> transactions = transactionService.getTransactionsOfOrder(newOrder);
                log.info(">>> Passed get transactions");

                //cập nhật danh sách transactions cho đơn hàng
                newOrder = orderService.updateOrderTransactions(newOrder, transactions);
                log.info(">>> Passed update transactions for order");

                newOrder = orderService.updateOrderStatus(newOrder, OrderStatus.PAID);
                log.info(">>> Passed update order status");

                //tạo đơn hàng thật ở trên giao hàng nhanh
                Map<String, String> createOrderShippingResponse = ghnService.createOrderShippingResponseToDto(
                        newOrder, transactionRepository.findAllByOrder(newOrder).getLast().getPayment()
                );

                //lưu orderShippingCode vào đơn hàng
                String orderShippingCode = createOrderShippingResponse.get("orderCode");
                log.info(">>> Passed get orderShippingCode: {}", orderShippingCode);
                newOrder = orderService.updateOrderCode(orderShippingCode, newOrder);
                log.info(">>> Passed set Order Code");

                //lưu phí dịch vụ vào đơn hàng
                String totalServiceFee = createOrderShippingResponse.get("totalFee");
                log.info(">>> Passed get totalServiceFee: {}", totalServiceFee);
                orderService.updateShippingFee(newOrder, totalServiceFee);

                //cập nhật lại tiền của transaction mới nhất
                transactionService.updateAmount(transactions.getLast(), newOrder.getPrice().add(newOrder.getShippingFee()));

                SystemWallet systemWallet = systemWalletService.createEscrowRecordForWalletPayment(newOrder, totalServiceFee);
                newOrder = orderService.updateSystemWallet(systemWallet, newOrder);
            }
            //tạo hoá đơn
            Invoice newInvoice = invoiceService.createInvoiceInstance(newOrder, "Không có ghi chú", 0);

            //tạo mã hoá đơn
            invoiceService.generateInvoice(newInvoice.getId());

            //cập nhật trạng thái bài đăng bán sản phẩm
            postProductService.updateSoldStatus(true, postProduct);

            //tạo response
            responseData = orderMapper.toDto(newOrder);
            log.info(">>> Passed created response");

            log.info(">>> Build response");
            response = responseMapper.toDto(
                    true,
                    "PLACE ORDERED SUCCESS",
                    responseData,
                    null
            );

            log.info(">>> [END] placeOrder success");
            return ResponseEntity.status(HttpStatus.OK.value()).body(response);
        } catch (Exception e) {
//            newOrder.getPostProduct().setSold(false);
            throw e;
        }
    }

    @Operation(
            summary = "Get wallet transaction history",
            description = """
                        Retrieves a paginated list of wallet transactions for the currently authenticated user.
                        This endpoint supports pagination through 'page' and 'size' query parameters.
                        Each record in the result includes transaction details such as:
                        - Transaction ID
                        - Type (credit/debit)
                        - Amount
                        - Status
                        - Timestamp
                        - ...
                        Use this API to display a user's transaction history in their dashboard or account page.
                    """
    )
    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER')")
    @GetMapping("/transaction-history")
    public ResponseEntity<?> getWalletTransactionHistory(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        try {
            Buyer buyer = buyerService.getCurrentUser();
            Page<WalletTransaction> transactions = walletService.getTransactionHistory(buyer, page, size);

            Page<WalletTransactionResponse> responsePage = transactions.map(walletMapper::toTransactionResponse);

            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET ALL WALLET TRANSACTION SUCCESSFULLY.",
                    responsePage, null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "GET ALL WALLET TRANSACTION SUCCESSFULLY.",
                    null, e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Add a product post to the buyer's wish-list",
            description = """
                    This endpoint allows an authenticated **buyer** to add a product post 
                    (`PostProduct`) to their personal wish-list.
                    
                    - The buyer must be logged in.
                    - The product (`PostProduct`) must exist and be active.
                    - A seller **cannot** add their own product to their own wish-list (for fairness and data integrity).
                    - If the product is already in the buyer's wish-list, the service may prevent duplication or update the record, depending on business logic.
                    
                    **Use case:**  
                    Buyers use this API to save or bookmark products they are interested in purchasing later.
                    """
    )
    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER')")
    @PostMapping("/wish-list")
    public ResponseEntity<?> addProductToWishList(@RequestBody WishListRequest request) {
        log.info(">>> [Buyer Controller] Add product to wish list: Started.");
        try {
            Buyer buyer = buyerService.getCurrentUser();
            log.info(">>> [Buyer Controller] Buyer info: {}", buyer.getUsername());

            log.info(">>> [Buyer Controller] Post product id: {}", request.getPostId());
            PostProduct postProduct = postProductService.getPostProductById(request.getPostId());
            log.info(">>> [Buyer Controller] Post product: {}", postProduct);

            if (buyer.getSeller() == postProduct.getSeller()) {
                throw new IllegalArgumentException("Seller can not add your product into your wish-listing.");
            }

            WishListing wishListing = wishListMapper.toEntity(request, buyer, postProduct);

            WishListing savedWishList = wishListingService.addWishList(wishListing);

            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "ADD PRODUCT TO WISH LISTING SUCCESSFULLY.",
                    wishListMapper.toDto(savedWishList), null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "ADD PRODUCT TO WISH LISTING FAILED.",
                    null, e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Remove a product from the buyer's wish list",
            description = """
                    This endpoint allows an authenticated **buyer** to remove a product post 
                    from their personal wish list.
                    
                    - The `wishId` must correspond to an existing wish-list entry.
                    - The buyer must own the wish-list entry; otherwise, access will be denied.
                    - If the wish-list item does not exist or has already been removed, the API will return an appropriate error message.
                    
                    **Use case:**  
                    Buyers use this endpoint when they no longer wish to keep a product in their saved wish-list.
                    """
    )
    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER')")
    @PostMapping("/remove-wish-list/{wishId}")
    public ResponseEntity<?> removeWishList(@PathVariable(name = "wishId") long id) {
        try {
            wishListingService.removePostProduct(id);
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "REMOVE POST PRODUCT FROM WISH LIST SUCCESSFULLY.",
                    null, null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "REMOVE POST PRODUCT FROM WISH LIST FAILED.",
                    null, e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Retrieve the buyer's wish list",
            description = """
                    This endpoint returns a paginated list of the buyer's wish-list items.
                    
                    - The buyer must be logged in.
                    - Results can be optionally filtered by **priority** (e.g., HIGH, MEDIUM, LOW).
                    - If no priority is specified, all wish-list items are returned.
                    - Supports pagination via `page` and `size` parameters.
                    
                    **Use case:**  
                    Buyers use this endpoint to view and manage the list of product posts they have added to their wish-list.
                    """
    )
    @GetMapping("/wish-list")
    public ResponseEntity<?> getWishList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "priority", required = false) WishListPriority priority
    ) {
        try {
            Buyer buyer = buyerService.getCurrentUser();
            Page<WishListing> wishListings = wishListingService.getWishList(buyer, page, size, priority);
            Page<WishListingResponse> mapped = wishListings.map(wishListMapper::toDto);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("content", mapped.getContent());
            data.put("pageNumber", mapped.getNumber());
            data.put("pageSize", mapped.getSize());
            data.put("totalElements", mapped.getTotalElements());
            data.put("totalPages", mapped.getTotalPages());
            data.put("first", mapped.isFirst());
            data.put("last", mapped.isLast());
            data.put("hasNext", mapped.hasNext());
            data.put("hasPrevious", mapped.hasPrevious());

            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET WISH LIST SUCCESSFULLY.",
                    data,
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "GET WISH LIST FAILED.",
                    null,
                    e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Retrieve paginated list of buyers",
            description = """
                    This endpoint allows an **administrator** to retrieve a paginated list of all registered buyers in the system. 
                    The request supports pagination parameters (`page`, `size`) and returns a structured response containing 
                    buyer information and metadata.
                    
                    **Access Control:** Only users with the role `ROLE_ADMIN` can access this endpoint.
                    
                    **Response:**
                    - On success: returns a paginated list of `BuyerResponse` objects with a success message.
                    - On failure: returns an error response with failure status and message details.
                    """
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<?> getBuyerList(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        try {
            Page<Buyer> listBuyer = buyerService.getListBuyers(page, size);
            Page<BuyerResponse> response = listBuyer.map(buyerMapper::toDto);
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET LIST BUYERS SUCCESSFULLY.",
                    response, null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "GET LIST BUYERS FAILED.",
                    null, e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Buyer confirms order receipt",
            description = """
                    Allows a buyer to confirm that they have received their order, 
                    which updates the order status from DELIVERED to COMPLETED.
                    
                    **Workflow:**
                    1. Buyer calls this endpoint after receiving the order
                    2. System validates:
                       - Order exists
                       - Order belongs to the authenticated buyer
                       - Order status is DELIVERED (not already COMPLETED)
                    3. System updates order status to COMPLETED
                    4. Returns updated order information
                    
                    **Business Rules:**
                    - Only the buyer who placed the order can confirm it
                    - Order must be in DELIVERED status
                    - If order is already COMPLETED, returns the order without changes
                    
                    **Path Parameters:**
                    - **orderId** *(Long, required)* - The unique identifier of the order to confirm
                    
                    **Response:**
                    Returns the updated order object with status = COMPLETED
                    
                    **Error Cases:**
                    - Order not found → 404
                    - Order does not belong to buyer → 403
                    - Order status is not DELIVERED → 400
                    
                    **Permissions:** Requires ROLE_BUYER authentication.
                    **Example:** PUT /api/v1/buyer/orders/123/confirm
                    """,
            parameters = {
                    @Parameter(
                            name = "orderId",
                            description = "The ID of the order to confirm",
                            required = true,
                            example = "123"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Order confirmed successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RestResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                        "success": true,
                                                        "message": "ORDER CONFIRMED SUCCESSFULLY",
                                                        "data": {
                                                            "id": 123,
                                                            "status": "COMPLETED",
                                                            ...
                                                        }
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Order status is not DELIVERED or order already completed"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Order does not belong to this buyer"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Order not found"
                    )
            }
    )
    @PreAuthorize("hasRole('ROLE_BUYER')")
    @PutMapping("/orders/{orderId}/confirm")
    public ResponseEntity<?> confirmOrder(
            @PathVariable(name = "orderId") Long orderId
    ) {
        try {
            log.info(">>> [BuyerController] confirmOrder - orderId: {}", orderId);
            
            // Lấy buyer hiện tại
            Buyer buyer = buyerService.getCurrentUser();
            log.info(">>> [BuyerController] Current buyer: {}", buyer.getBuyerId());
            
            // Xác nhận đơn hàng
            Order confirmedOrder = orderService.confirmOrder(orderId, buyer);
            
            // Cập nhật system wallet endAt nếu có
            if (confirmedOrder.getSystemWallet() != null) {
                systemWalletService.updateTimeWhenBuyerReceivedProduct(confirmedOrder.getSystemWallet());
                log.info(">>> [BuyerController] Updated system wallet endAt for order {}", orderId);
            }
            
            // Xử lý transaction cho COD nếu chưa có
            if (confirmedOrder.getTransactions() != null && !confirmedOrder.getTransactions().isEmpty()) {
                String paymentGateway = confirmedOrder.getTransactions().getLast().getPayment().getGatewayName();
                
                // Kiểm tra xem transaction SUCCESS đã có chưa
                boolean hasSuccessTransaction = confirmedOrder.getTransactions().stream()
                        .anyMatch(t -> t.getStatus().equals(TransactionStatus.SUCCESS));
                
                if ("COD".equalsIgnoreCase(paymentGateway) && !hasSuccessTransaction) {
                    Transaction transaction = transactionService.createTransaction(
                            confirmedOrder,
                            TransactionStatus.SUCCESS,
                            confirmedOrder.getTransactions().getLast().getPayment()
                    );
                    log.info(">>> [BuyerController] Created transaction for COD order {}", orderId);
                }
            }
            
            OrderResponse responseData = orderMapper.toDto(confirmedOrder);
            
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "ORDER CONFIRMED SUCCESSFULLY",
                    responseData,
                    null
            ));
            
        } catch (OrderNotFound e) {
            log.error(">>> [BuyerController] Order not found: {}", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseMapper.toDto(
                    false,
                    "ORDER NOT FOUND",
                    null,
                    e.getMessage()
            ));
        } catch (Exception e) {
            log.error(">>> [BuyerController] Error confirming order: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseMapper.toDto(
                    false,
                    "ERROR CONFIRMING ORDER: " + e.getMessage(),
                    null,
                    e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Get total number of buyers",
            description = "This endpoint returns the total number of registered buyers in the system. " +
                    "Accessible only by users with the ADMIN role."
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/total-buyers")
    public ResponseEntity<?> getTotalBuyers() {
        int total = buyerService.getTotalBuyers();
        return ResponseEntity.ok(responseMapper.toDto(
                true,
                "GET TOTAL BUYERS SUCCESSFULLY.",
                total, null
        ));
    }
}
