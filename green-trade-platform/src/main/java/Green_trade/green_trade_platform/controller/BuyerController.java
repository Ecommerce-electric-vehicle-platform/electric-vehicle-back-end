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
@Tag(name = "Buyer Management", description = "APIs for buyer operations including profile management, wallet, orders, wish list, and transaction history")
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
                    Allows a buyer to create their initial profile including personal information and avatar image.
                    
                    ## Workflow:
                    1. Buyer submits profile data (name, address, phone) and avatar image via multipart form
                    2. System validates all input fields (phone format, required fields, etc.)
                    3. Avatar image is uploaded to Cloudinary cloud storage
                    4. Profile information is saved to database
                    5. Returns complete buyer profile with avatar URL
                    
                    ## Validations:
                    - Full name: Required
                    - Street address: Required, max 255 characters
                    - Phone number: Required, Vietnamese format (0XXXXXXXXX or +84XXXXXXXXX)
                    - Date of birth: Format dd-MM-yyyy, must be valid date
                    - Avatar file: Required, max 5MB, formats: JPEG, PNG, GIF
                    
                    ## Security:
                    - Requires authentication (ROLE_BUYER or ROLE_SELLER)
                    - Only owner can upload their own profile
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Multipart form data with profile information and avatar image",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE
                    )
            ),
            tags = {"Buyer Profile Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile uploaded successfully",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "UPLOAD PROFILE SUCCESS.",
                                              "data": {
                                                "buyerId": 1,
                                                "username": "buyer123",
                                                "fullName": "Nguyễn Văn A",
                                                "email": "buyer@example.com",
                                                "phoneNumber": "0912345678",
                                                "avatarUrl": "https://cloudinary.com/avatar.jpg",
                                                "shippingAddress": "123 Đường ABC, Phường 1, Quận 1, TP.HCM",
                                                "dob": "1990-01-01",
                                                "active": true,
                                                "createdAt": "2024-01-15T10:00:00",
                                                "updatedAt": "2024-01-15T10:00:00"
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Validation errors",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": false,
                                              "message": "Validation failed",
                                              "data": null,
                                              "error": {
                                                "fullName": "Full name cannot be blank",
                                                "phoneNumber": "Phone number must be valid"
                                              }
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
                    responseCode = "413",
                    description = "Payload Too Large - File size exceeds 5MB limit"
            ),
            @ApiResponse(
                    responseCode = "415",
                    description = "Unsupported Media Type - Invalid file format"
            )
    })
    @PostMapping(
            value = "/upload-profile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadBuyerProfile(
            @Parameter(description = "Profile information including name, address, phone, and date of birth", required = true)
            @Valid @ModelAttribute ProfileRequest profileRequest,
            @Parameter(description = "Avatar image file (JPEG/PNG/GIF, max 5MB)", required = true)
            @RequestPart(value = "avatar_url", required = true) MultipartFile avatarFile
    ) throws IOException {
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
                    Update existing buyer profile information including personal details and optional avatar image.
                    
                    ## Workflow:
                    1. Buyer submits updated profile fields (any or all fields can be updated)
                    2. Optionally includes new avatar image to replace existing one
                    3. System validates updated information
                    4. Old avatar is deleted from cloud storage if new one is provided
                    5. Profile is updated in database
                    6. Returns complete updated profile
                    
                    ## Update Rules:
                    - All fields are optional - only provided fields will be updated
                    - Avatar is optional - if provided, replaces existing avatar
                    - Phone number must follow Vietnamese format if provided
                    - Date of birth must be valid date in format dd-MM-yyyy if provided
                    
                    ## Security:
                    - Requires authentication (ROLE_BUYER or ROLE_SELLER)
                    - Only profile owner can update their information
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated profile information and optional new avatar image",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE
                    )
            ),
            tags = {"Buyer Profile Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile updated successfully",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "UPDATED PROFILE SUCCESSFULLY",
                                              "data": {
                                                "buyerId": 1,
                                                "username": "buyer123",
                                                "fullName": "Nguyễn Văn A (Updated)",
                                                "email": "buyer@example.com",
                                                "phoneNumber": "0987654321",
                                                "avatarUrl": "https://cloudinary.com/new-avatar.jpg",
                                                "shippingAddress": "456 Đường XYZ, Phường 2, Quận 3, TP.HCM",
                                                "dob": "1990-01-01",
                                                "active": true,
                                                "createdAt": "2024-01-15T10:00:00",
                                                "updatedAt": "2024-01-16T15:30:00"
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Validation errors",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": false,
                                              "message": "Validation failed",
                                              "data": null,
                                              "error": {
                                                "phoneNumber": "Phone number must be valid format"
                                              }
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
                    description = "Not Found - Profile not found"
            )
    })
    @PutMapping(
            value = "/update-profile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER')")
    public ResponseEntity<RestResponse<BuyerResponse, Object>> updateProfile(
            @Parameter(description = "Updated profile information (all fields optional)", required = true)
            @Valid @ModelAttribute UpdateBuyerProfileRequest updateProfileRequest,
            @Parameter(description = "New avatar image file (optional, replaces existing)", required = false)
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
                    Retrieve the complete profile information of the currently authenticated buyer.
                    
                    ## Workflow:
                    1. Client sends request with JWT token in Authorization header
                    2. System validates token and extracts buyer identity
                    3. System fetches complete buyer profile from database
                    4. Returns profile with all details including avatar URL
                    
                    ## Response Includes:
                    - Personal information (name, email, phone)
                    - Shipping address details
                    - Avatar image URL
                    - Account status and timestamps
                    
                    ## Security:
                    - Requires valid JWT Bearer token
                    - Only returns authenticated user's own profile
                    - No access to other users' profiles
                    """,
            tags = {"Buyer Profile Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "Get user profile successfully.",
                                              "data": {
                                                "buyerId": 1,
                                                "username": "buyer123",
                                                "fullName": "Nguyễn Văn A",
                                                "email": "buyer@example.com",
                                                "phoneNumber": "0912345678",
                                                "avatarUrl": "https://cloudinary.com/avatar.jpg",
                                                "shippingAddress": "123 Đường ABC, Phường 1, Quận 1, TP.HCM",
                                                "dob": "1990-01-01",
                                                "active": true,
                                                "createdAt": "2024-01-15T10:00:00",
                                                "updatedAt": "2024-01-15T10:00:00"
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or expired token",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": false,
                                              "message": "Unauthorized - Please login",
                                              "data": null,
                                              "error": "Invalid or expired token"
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
                                    value = """
                                            {
                                              "success": false,
                                              "message": "Error occur during get user profile.",
                                              "data": null,
                                              "error": "Internal server error"
                                            }
                                            """
                            )
                    )
            )
    })
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
            summary = "Get user wallet information",
            description = """
                    Retrieve wallet information for the currently authenticated buyer including balance and transaction history access.
                    
                    ## Workflow:
                    1. Client sends request with JWT token in Authorization header
                    2. System validates token and extracts user identity
                    3. System fetches wallet associated with the authenticated user
                    4. Returns wallet details including current balance
                    
                    ## Wallet Information Includes:
                    - Wallet ID
                    - Current balance (in VND)
                    - Associated user information
                    - Wallet status (active/inactive)
                    - Creation and update timestamps
                    
                    ## Use Cases:
                    - Display wallet balance on user dashboard
                    - Check available funds before making purchase
                    - Verify wallet status before transactions
                    
                    ## Security:
                    - Requires valid JWT Bearer token
                    - Only returns authenticated user's own wallet
                    - Balance information is protected
                    """,
            tags = {"Buyer Wallet Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Wallet information retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "Get wallet's information successfully.",
                                              "data": {
                                                "walletId": 1,
                                                "balance": 1500000.00,
                                                "userId": 1,
                                                "username": "buyer123",
                                                "status": "ACTIVE",
                                                "createdAt": "2024-01-15T10:00:00",
                                                "updatedAt": "2024-11-10T14:30:00"
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or expired token",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": false,
                                              "message": "Unauthorized - Please login",
                                              "data": null,
                                              "error": "Invalid or expired token"
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
                                    value = """
                                            {
                                              "success": false,
                                              "message": "Get wallet information failed.",
                                              "data": null,
                                              "error": "Internal server error"
                                            }
                                            """
                            )
                    )
            )
    })
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
            summary = "Add product to wish list",
            description = """
                    Add a product post to buyer's personal wish list for future tracking and potential purchase.
                    
                    ## Workflow:
                    1. System authenticates the buyer from JWT token
                    2. Validates that product exists and is active
                    3. Prevents sellers from adding their own products
                    4. Creates wish list entry with specified priority
                    5. Returns saved wish list item details
                    
                    ## Business Rules:
                    - Product must exist and be available
                    - Buyer cannot add already wishlisted products (duplicate prevention)
                    - Sellers cannot wishlist their own products
                    - Each wish list item has a priority level (HIGH/MEDIUM/LOW)
                    
                    ## Use Cases:
                    - Save interesting products for later review
                    - Track products before making purchase decision
                    - Compare multiple products
                    - Maintain shopping list
                    
                    ## Security:
                    - Requires authentication (ROLE_BUYER or ROLE_SELLER)
                    - Only authenticated user can add to their wish list
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Wish list request containing product ID and priority",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = WishListRequest.class),
                            examples = @ExampleObject(
                                    name = "Example Request",
                                    value = """
                                            {
                                              "postId": 123,
                                              "priority": "HIGH"
                                            }
                                            """
                            )
                    )
            ),
            tags = {"Buyer Wish List Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Product added to wish list successfully",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "ADD PRODUCT TO WISH LISTING SUCCESSFULLY.",
                                              "data": {
                                                "wishId": 1,
                                                "postProductId": 123,
                                                "productName": "iPhone 13 Pro Max",
                                                "productPrice": 15000000,
                                                "priority": "HIGH",
                                                "buyerId": 1,
                                                "createdAt": "2024-11-12T10:00:00"
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Validation error or seller trying to add own product",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Self-Wishlist Error",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "message": "ADD PRODUCT TO WISH LISTING FAILED.",
                                                      "data": null,
                                                      "error": "Seller can not add your product into your wish-listing."
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "Duplicate Entry",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "message": "ADD PRODUCT TO WISH LISTING FAILED.",
                                                      "data": null,
                                                      "error": "Product is already in your wish list"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Product not found",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": false,
                                              "message": "ADD PRODUCT TO WISH LISTING FAILED.",
                                              "data": null,
                                              "error": "Product with ID 123 not found"
                                            }
                                            """
                            )
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER')")
    @PostMapping("/wish-list")
    public ResponseEntity<?> addProductToWishList(
            @Parameter(description = "Wish list request with product ID and priority level", required = true)
            @RequestBody WishListRequest request
    ) {
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
            summary = "Remove product from wish list",
            description = """
                    Remove a specific product from buyer's wish list using the wish list entry ID.
                    
                    ## Workflow:
                    1. System validates wish list entry exists
                    2. Verifies ownership (only entry owner can remove)
                    3. Deletes wish list entry from database
                    4. Returns success confirmation
                    
                    ## Business Rules:
                    - Wish list entry must exist
                    - Only the owner can remove their wish list items
                    - Removal is permanent and cannot be undone
                    
                    ## Use Cases:
                    - Clean up wish list after purchase
                    - Remove no longer interested products
                    - Manage wish list size
                    
                    ## Security:
                    - Requires authentication (ROLE_BUYER or ROLE_SELLER)
                    - Access control validates ownership
                    """,
            tags = {"Buyer Wish List Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Product removed from wish list successfully",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "REMOVE POST PRODUCT FROM WISH LIST SUCCESSFULLY.",
                                              "data": null,
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Removal failed",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": false,
                                              "message": "REMOVE POST PRODUCT FROM WISH LIST FAILED.",
                                              "data": null,
                                              "error": "Error message details"
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
                    responseCode = "403",
                    description = "Forbidden - Not authorized to remove this wish list item"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Wish list entry not found"
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER')")
    @PostMapping("/remove-wish-list/{wishId}")
    public ResponseEntity<?> removeWishList(
            @Parameter(description = "Wish list entry ID to be removed", required = true, example = "1")
            @PathVariable(name = "wishId") long id
    ) {
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
            summary = "Get buyer's wish list",
            description = """
                    Retrieve a paginated list of products in the buyer's wish list with optional priority filtering.
                    
                    ## Workflow:
                    1. System authenticates buyer from JWT token
                    2. Fetches wish list entries associated with buyer
                    3. Applies priority filter if specified (HIGH/MEDIUM/LOW)
                    4. Returns paginated results with product details
                    
                    ## Query Parameters:
                    - **page**: Page number (0-based indexing), default: 0
                    - **size**: Number of items per page, default: 10
                    - **priority**: Filter by priority level (optional)
                    
                    ## Response Includes:
                    - List of wish list items with product details
                    - Pagination metadata (page number, size, total elements, total pages)
                    - Product information (name, price, images, seller)
                    - Wish list entry metadata (priority, created date)
                    
                    ## Use Cases:
                    - View all saved/bookmarked products
                    - Filter high-priority items for quick access
                    - Manage and organize wish list
                    - Compare products before purchase
                    
                    ## Security:
                    - Requires authentication (ROLE_BUYER or ROLE_SELLER)
                    - Only returns authenticated user's wish list
                    """,
            tags = {"Buyer Wish List Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Wish list retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "GET WISH LIST SUCCESSFULLY.",
                                              "data": {
                                                "content": [
                                                  {
                                                    "wishId": 1,
                                                    "postProductId": 123,
                                                    "productName": "iPhone 13 Pro Max",
                                                    "productPrice": 15000000,
                                                    "productImage": "https://cloudinary.com/product.jpg",
                                                    "priority": "HIGH",
                                                    "sellerName": "Seller ABC",
                                                    "createdAt": "2024-11-10T10:00:00"
                                                  }
                                                ],
                                                "pageNumber": 0,
                                                "pageSize": 10,
                                                "totalElements": 5,
                                                "totalPages": 1
                                              },
                                              "error": null
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
                    responseCode = "500",
                    description = "Internal Server Error"
            )
    })
    @GetMapping("/wish-list")
    public ResponseEntity<?> getWishList(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(name = "size", defaultValue = "10") int size,
            @Parameter(description = "Filter by priority level (HIGH/MEDIUM/LOW)", required = false)
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
            summary = "Get list of all buyers (Admin only)",
            description = """
                    Retrieve a paginated list of all registered buyers in the system - Admin access only.
                    
                    ## Workflow:
                    1. System verifies admin authentication and authorization
                    2. Fetches all buyer accounts from database
                    3. Applies pagination to results
                    4. Returns buyer list with pagination metadata
                    
                    ## Response Includes:
                    - Buyer profiles (ID, username, email, full name, phone)
                    - Avatar URLs
                    - Shipping addresses
                    - Account status (active/inactive)
                    - Registration dates
                    - Pagination metadata
                    
                    ## Use Cases:
                    - Admin dashboard to view all buyers
                    - User management and monitoring
                    - Generate buyer reports
                    - Account verification and support
                    
                    ## Security:
                    - **Admin only** - Requires ROLE_ADMIN
                    - No access for regular buyers or sellers
                    """,
            tags = {"Admin - Buyer Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Buyer list retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "GET LIST BUYERS SUCCESSFULLY.",
                                              "data": {
                                                "content": [
                                                  {
                                                    "buyerId": 1,
                                                    "username": "buyer123",
                                                    "email": "buyer@example.com",
                                                    "fullName": "Nguyễn Văn A",
                                                    "phoneNumber": "0912345678",
                                                    "avatarUrl": "https://cloudinary.com/avatar1.jpg",
                                                    "active": true,
                                                    "createdAt": "2024-01-15T10:00:00"
                                                  }
                                                ],
                                                "pageable": {
                                                  "pageNumber": 0,
                                                  "pageSize": 10
                                                },
                                                "totalElements": 50,
                                                "totalPages": 5,
                                                "last": false,
                                                "first": true
                                              },
                                              "error": null
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
                    responseCode = "403",
                    description = "Forbidden - Admin access required",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": false,
                                              "message": "Access denied",
                                              "data": null,
                                              "error": "Admin privileges required"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error"
            )
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<?> getBuyerList(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
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
            summary = "Get total count of buyers (Admin only)",
            description = """
                    Retrieve the total number of registered buyers in the system - Admin access only.
                    
                    ## Workflow:
                    1. System verifies admin authentication and authorization
                    2. Queries database to count all buyer accounts
                    3. Returns total count as integer
                    
                    ## Use Cases:
                    - Admin dashboard statistics
                    - System monitoring and reporting
                    - Business analytics
                    - User growth tracking
                    
                    ## Security:
                    - **Admin only** - Requires ROLE_ADMIN
                    - No access for regular buyers or sellers
                    """,
            tags = {"Admin - Buyer Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Total buyers count retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "GET TOTAL BUYERS SUCCESSFULLY.",
                                              "data": 150,
                                              "error": null
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
                    responseCode = "403",
                    description = "Forbidden - Admin access required"
            )
    })
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
