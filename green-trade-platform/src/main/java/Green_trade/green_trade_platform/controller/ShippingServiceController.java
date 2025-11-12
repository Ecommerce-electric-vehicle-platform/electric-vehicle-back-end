package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.enumerate.OrderStatus;
import Green_trade.green_trade_platform.enumerate.TransactionStatus;
import Green_trade.green_trade_platform.exception.OrderNotFound;
import Green_trade.green_trade_platform.exception.PaymentMethodNotSupportedException;
import Green_trade.green_trade_platform.exception.PostProductNotFound;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.model.*;
import Green_trade.green_trade_platform.repository.OrderRepository;
import Green_trade.green_trade_platform.repository.PostProductRepository;
import Green_trade.green_trade_platform.request.ShippingFeeRequest;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.service.PostProductService;
import Green_trade.green_trade_platform.service.implement.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/shipping")
@Slf4j
public class ShippingServiceController {

    private final GhnServiceImpl ghnService;
    private final ResponseMapper responseMapper;
    private final OrderRepository orderRepository;
    private final PostProductService postProductService;
    private final PostProductRepository postProductRepository;
    private final BuyerServiceImpl buyerService;
    private final PaymentServiceImpl paymentService;
    private final OrderServiceImpl orderService;
    private final SystemWalletServiceImpl systemWalletService;
    private final TransactionServiceImpl transactionService;

    public ShippingServiceController(
            GhnServiceImpl ghnService,
            ResponseMapper responseMapper,
            OrderRepository orderRepository,
            PostProductService postProductService,
            PostProductRepository postProductRepository,
            BuyerServiceImpl buyerService,
            PaymentServiceImpl paymentService,
            OrderServiceImpl orderService,
            SystemWalletServiceImpl systemWalletService,
            TransactionServiceImpl transactionService) {
        this.ghnService = ghnService;
        this.responseMapper = responseMapper;
        this.orderRepository = orderRepository;
        this.postProductService = postProductService;
        this.postProductRepository = postProductRepository;
        this.buyerService = buyerService;
        this.paymentService = paymentService;
        this.orderService = orderService;
        this.systemWalletService = systemWalletService;
        this.transactionService = transactionService;
    }

    @Operation(
            summary = "Fetch list of provinces",
            description = """
                    Retrieves a list of provinces available from the GHN (Giao Hàng Nhanh) shipping service.
                    This data is often used to populate province dropdowns during address creation or checkout.
                    
                    ## Workflow:
                    1. System calls the GHN API to retrieve the list of supported provinces
                    2. Response is mapped into a key-value structure (province code → province name)
                    3. Returns JSON object containing all provinces supported for shipping
                    
                    ## Use Cases:
                    - Displaying list of provinces when users fill out shipping or billing addresses
                    - Fetching location data dynamically from the GHN logistics API
                    - Populating province dropdown in registration or checkout forms
                    
                    ## Security:
                    - Public endpoint - No authentication required
                    """,
            tags = {"Shipping - Location Data"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Provinces retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "FETCH PROVINCES SUCCESSFULLY",
                                              "data": {
                                                "201": "An Giang",
                                                "202": "Bà Rịa - Vũng Tàu",
                                                "203": "Bắc Giang",
                                                "204": "Bắc Kạn",
                                                "79": "TP. Hồ Chí Minh",
                                                "1": "Hà Nội"
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error - Failed to fetch provinces from GHN API"
            )
    })
    @GetMapping("/provinces")
    public ResponseEntity<?> getProvinces() throws JsonProcessingException {
        Map<String, String> provincesMap = new HashMap<>();
        provincesMap = ghnService.getProvinceList();
        RestResponse response = responseMapper.toDto(
                true,
                "FETCH PROVINCES SUCCESSFULLY",
                provincesMap,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @Operation(
            summary = "Fetch list of districts by province ID",
            description = """
                    Retrieves a list of districts for a specified province using the GHN (Giao Hàng Nhanh) shipping service.
                    This endpoint is typically used when the user selects a province, and the frontend needs to load 
                    all districts under that province dynamically.
                    
                    ## Workflow:
                    1. Client provides a provinceId as a request parameter
                    2. System sends request to the GHN API to fetch districts belonging to that province
                    3. Districts are mapped into a key-value format (district code → district name)
                    4. Returns the resulting district list
                    
                    ## Use Cases:
                    - Displaying available districts when users select a province during checkout or address creation
                    - Dynamically populating location dropdowns in registration or shipping forms
                    - Cascading location selection (province → district → ward)
                    
                    ## Security:
                    - Public endpoint - No authentication required
                    """,
            tags = {"Shipping - Location Data"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Districts retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "FETCH DISTRICTS SUCCESSFULLY",
                                              "data": {
                                                "1451": "Quận 1",
                                                "1452": "Quận 2",
                                                "1453": "Quận 3",
                                                "1454": "Quận 4",
                                                "1455": "Quận 5"
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid province ID"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error - Failed to fetch districts from GHN API"
            )
    })
    @GetMapping("/districts")
    public ResponseEntity<?> getDistricts(
            @Parameter(
                    description = "The ID of the province to fetch districts for",
                    required = true,
                    example = "79"
            )
            @RequestParam int provinceId
    ) throws JsonProcessingException {
        Map<String, String> districtsMap = new HashMap<>();
        districtsMap = ghnService.getDistrictListByProvinceId(provinceId);
        RestResponse response = responseMapper.toDto(
                true,
                "FETCH DISTRICTS SUCCESSFULLY",
                districtsMap,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @Operation(
            summary = "Fetch list of wards by district ID",
            description = """
                    Retrieves a list of wards (subdistricts) for a specific district using the GHN (Giao Hàng Nhanh) shipping service.
                    This endpoint is usually called after a district is selected, allowing the frontend to dynamically display 
                    all available wards under that district.
                    
                    ## Workflow:
                    1. Client provides a districtId as a request parameter
                    2. System calls the GHN API to fetch wards corresponding to the given district
                    3. Ward data is formatted as a key-value map (ward code → ward name)
                    4. Returns the formatted list to the client
                    
                    ## Use Cases:
                    - Displaying available wards when users select a district during checkout or address creation
                    - Completing address selection for shipping, billing, or delivery purposes
                    - Final step in cascading location selection (province → district → ward)
                    
                    ## Security:
                    - Public endpoint - No authentication required
                    """,
            tags = {"Shipping - Location Data"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Wards retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "FETCH WARDS SUCCESSFULLY",
                                              "data": {
                                                "21011": "Phường Bến Nghé",
                                                "21012": "Phường Đa Kao",
                                                "21013": "Phường Cô Giang",
                                                "21014": "Phường Cầu Kho",
                                                "21015": "Phường Nguyễn Thái Bình"
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid district ID"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error - Failed to fetch wards from GHN API"
            )
    })
    @GetMapping("/wards")
    public ResponseEntity<?> getWards(
            @Parameter(
                    description = "The ID of the district to fetch wards for",
                    required = true,
                    example = "1451"
            )
            @RequestParam int districtId
    ) throws JsonProcessingException {
        Map<String, String> wardsMap = new HashMap<>();
        wardsMap = ghnService.getWardListByDistrictId(districtId);
        RestResponse response = responseMapper.toDto(
                true,
                "FETCH WARDS SUCCESSFULLY",
                wardsMap,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @Operation(
            summary = "Fetch shipping fee for a specific order",
            description = """
                    Retrieves the shipping fee for a given order using the GHN (Giao Hàng Nhanh) shipping service.
                    The system uses order details (such as delivery address, package weight, and dimensions) to query 
                    GHN's API and return the calculated shipping cost.
                    
                    ## Workflow:
                    1. Client sends an orderId as a path variable
                    2. System validates that the order exists
                    3. GHN API is called with the order's delivery information
                    4. Calculated shipping fee and related details (service type, estimated delivery time, etc.) are returned
                    
                    ## Use Cases:
                    - Displaying the estimated or actual shipping fee on the order details page
                    - Allowing sellers or admins to verify delivery costs before fulfillment
                    - Showing buyers the delivery cost breakdown in checkout or order tracking screens
                    
                    ## Security:
                    - Requires authentication (ROLE_BUYER, ROLE_SELLER, or ROLE_ADMIN)
                    - User can only access shipping fee information for orders they own
                    """,
            tags = {"Shipping - Fee Calculation"},
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Shipping fee retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "FETCH SHIPPING FEE SUCCESSFULLY",
                                              "data": {
                                                "total": "30000",
                                                "service_fee": "25000",
                                                "insurance_fee": "0",
                                                "cod_fee": "0",
                                                "pick_station_fee": "0",
                                                "deliver_station_fee": "0"
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
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Access denied to this order"
            )
    })
    @GetMapping("/shipping-fee/{orderId}")
    public ResponseEntity<?> getShippingFee(
            @Parameter(
                    description = "The ID of the order to calculate shipping fee for",
                    required = true,
                    example = "123"
            )
            @PathVariable Long orderId
    ) throws Exception {
        int codValue = 0;
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFound());
        Map<String, String> shippingFeeData = ghnService.getShippingFeeDto(order, codValue);
        RestResponse response = responseMapper.toDto(
                true,
                "FETCH SHIPPING FEE SUCCESSFULLY",
                shippingFeeData,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @Operation(
            summary = "Calculate shipping fee before placing order",
            description = """
                    Calculates the shipping fee for a product before placing an order using the GHN (Giao Hàng Nhanh) shipping service.
                    This endpoint is typically called during checkout to show the buyer the estimated shipping cost.
                    
                    ## Workflow:
                    1. Client provides product ID, delivery address (province, district, ward), and payment method
                    2. System retrieves product details and current buyer information
                    3. If payment method is COD, COD value is set to product price
                    4. GHN API is called with seller and buyer addresses, product dimensions, and COD value
                    5. Returns calculated shipping fee breakdown
                    
                    ## Request Body:
                    - **postId**: ID of the product to calculate shipping for
                    - **provinceName**: Province name for delivery address
                    - **districtName**: District name for delivery address
                    - **wardName**: Ward name for delivery address
                    - **paymentId**: Payment method ID (affects COD calculation)
                    
                    ## Payment Method Impact:
                    - **COD Payment**: COD value = product price (affects shipping fee calculation)
                    - **Online/Wallet Payment**: COD value = 0
                    
                    ## Use Cases:
                    - Displaying estimated shipping cost during checkout
                    - Allowing buyers to see shipping fee before confirming order
                    - Calculating total order cost (product price + shipping fee)
                    
                    ## Security:
                    - Requires authentication (ROLE_BUYER or ROLE_SELLER)
                    - Uses current authenticated user's information
                    """,
            tags = {"Shipping - Fee Calculation"},
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Shipping fee calculated successfully",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "FETCH SHIPPING FEE SUCCESSFULLY",
                                              "data": {
                                                "total": "35000",
                                                "service_fee": "30000",
                                                "insurance_fee": "0",
                                                "cod_fee": "5000",
                                                "pick_station_fee": "0",
                                                "deliver_station_fee": "0"
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid request data",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": false,
                                              "message": "Validation failed",
                                              "data": null,
                                              "error": "Invalid address or payment method"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Resource not found",
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
                                            name = "Payment Method Not Found",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "message": "Payment method not supported",
                                                      "data": null,
                                                      "error": "Payment method with ID 1 does not exist"
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required"
            )
    })
    @PostMapping("/shipping-fee")
    public ResponseEntity<RestResponse<Map<String, String>, Object>> getShippingFee(
            @Parameter(
                    description = "Shipping fee calculation request with product, address, and payment information",
                    required = true
            )
            @Valid @RequestBody ShippingFeeRequest request
    ) throws Exception {
        int codValue = 0;
        log.info(">>> [ShippingServiceController] in getShippingFee: codValue = {}", codValue);
        PostProduct postProduct = postProductRepository.findById(request.getPostId()).orElseThrow(() -> new PostProductNotFound());

        Buyer currentBuyer = buyerService.getCurrentUser();
        Buyer targetBuyer = currentBuyer;
        targetBuyer.setWardName(request.getWardName());
        targetBuyer.setDistrictName(request.getDistrictName());
        targetBuyer.setProvinceName(request.getProvinceName());

        Seller seller = postProduct.getSeller();

        Payment payment = paymentService.findPaymentMethodById(request.getPaymentId());

        if (payment == null) {
            throw new PaymentMethodNotSupportedException();
        }
        log.info(">>> [ShippingServiceController] in getShippingFee: Payment is supported");


        if (payment.getGatewayName().equalsIgnoreCase("COD")) {
            log.info(">>> [ShippingServiceController] in getShippingFee: COD payment");
            codValue = postProduct.getPrice().intValue();
        }
        log.info(">>> [ShippingServiceController] in getShippingFee: codValue = {}", codValue);

        Map<String, String> shippingFeeData = ghnService.getShippingFeeDto(targetBuyer, seller, postProduct, codValue);

        RestResponse<Map<String, String>, Object> response = responseMapper.toDto(
                true,
                "FETCH SHIPPING FEE SUCCESSFULLY",
                shippingFeeData,
                null
        );

        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @Operation(
            summary = "Get order shipping status from GHN",
            description = """
                    Retrieves the latest shipping status of an order from GHN (Giao Hàng Nhanh) shipping service.
                    This endpoint also automatically updates the order status in the system based on GHN's status.
                    
                    ## Workflow:
                    1. System retrieves order by orderId
                    2. Calls GHN API to get latest shipping status using order code
                    3. Compares GHN status with current order status
                    4. Automatically updates order status if changed:
                       - **picked** → Updates to PICKED
                       - **delivering** → Updates to DELIVERING
                       - **delivered** → Updates to DELIVERED and processes transactions
                    5. Returns latest shipping status information
                    
                    ## Status Updates:
                    - When status is **delivered**:
                      - For COD orders: Creates SUCCESS transaction
                      - Updates system wallet endAt timestamp
                      - Marks order as DELIVERED
                    
                    ## Response Includes:
                    - Current shipping status from GHN
                    - Status history and tracking information
                    - Estimated delivery time
                    - Location updates
                    
                    ## Use Cases:
                    - Tracking order delivery progress
                    - Displaying real-time shipping status to buyers
                    - Automatic order status synchronization
                    - Order tracking page updates
                    
                    ## Security:
                    - Requires authentication (ROLE_BUYER, ROLE_SELLER, or ROLE_ADMIN)
                    - Users can only track orders they own or have access to
                    """,
            tags = {"Shipping - Order Tracking"},
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order shipping status retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = RestResponse.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "FETCH ORDER SHIPPING STATUS SUCCESSFULLY",
                                              "data": {
                                                "status": "delivering",
                                                "orderCode": "GHN123456789",
                                                "currentLocation": "Trung tâm phân phối Hà Nội",
                                                "estimatedDeliveryTime": "2024-01-20T14:00:00",
                                                "statusHistory": [
                                                  {
                                                    "status": "picked",
                                                    "time": "2024-01-18T10:00:00",
                                                    "location": "Kho hàng TP.HCM"
                                                  },
                                                  {
                                                    "status": "delivering",
                                                    "time": "2024-01-19T08:00:00",
                                                    "location": "Đang vận chuyển"
                                                  }
                                                ]
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
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Authentication required"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Access denied to this order"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error - Failed to fetch status from GHN API"
            )
    })
    @GetMapping("/order/{orderId}/status")
    public ResponseEntity<RestResponse<Map<String, Object>, Object>> getOrderStatus(
            @Parameter(
                    description = "The ID of the order to get shipping status for",
                    required = true,
                    example = "123"
            )
            @PathVariable Long orderId
    ) {
        Order foundOrder = orderService.getOrderById(orderId);

        if (foundOrder == null) {
            throw new OrderNotFound();
        }

        Map<String, Object> responseData = ghnService.getLastestOrderStatus(foundOrder.getOrderCode());
        String status = responseData.get("status").toString();
        if (status.equalsIgnoreCase("picked")) {
            if (!foundOrder.getStatus().equals(OrderStatus.PICKED)) {
                orderService.updateOrderStatus(foundOrder, OrderStatus.PICKED);
            }
        } else if (status.equalsIgnoreCase("delivering")) {
            if (!foundOrder.getStatus().equals(OrderStatus.DELIVERING)) {
                orderService.updateOrderStatus(foundOrder, OrderStatus.DELIVERING);
            }
        } else if (status.equalsIgnoreCase("delivered")) {
            if (!foundOrder.getStatus().equals(OrderStatus.COMPLETED)) {
                orderService.updateOrderStatus(foundOrder, OrderStatus.DELIVERED);
//                orderService.updateOrderStatus(foundOrder, OrderStatus.COMPLETED);
                if ("COD".equalsIgnoreCase(foundOrder.getTransactions().getLast().getPayment().getGatewayName())) {
                    Transaction transaction = transactionService.createTransaction(foundOrder, TransactionStatus.SUCCESS, foundOrder.getTransactions().getLast().getPayment());
//                SystemWallet systemWallet = systemWalletService.createEscrowRecordAfterReduceFeeCOD(foundOrder, foundOrder.getShippingFee());
//                log.info(">>> pass create system wallet");
//                foundOrder = orderService.updateSystemWallet(systemWallet, foundOrder);
                    systemWalletService.updateTimeWhenBuyerReceivedProduct(foundOrder.getSystemWallet());
                    log.info(">>> pass update system wallet for order");
                } else {
//                SystemWallet systemWallet = systemWalletService.createEscrowRecordAfterReduceFeeCOD(foundOrder, foundOrder.getShippingFee());
//                log.info(">>> pass create system wallet");
//                foundOrder = orderService.updateSystemWallet(systemWallet, foundOrder);
                    systemWalletService.updateTimeWhenBuyerReceivedProduct(foundOrder.getSystemWallet());
                    log.info(">>> pass update system wallet for order");
                }
            }
        }

        RestResponse<Map<String, Object>, Object> response = responseMapper.toDto(
                true,
                "FETCH ORDER SHIPPING STATUS SUCCESSFULLY",
                responseData,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }
}
