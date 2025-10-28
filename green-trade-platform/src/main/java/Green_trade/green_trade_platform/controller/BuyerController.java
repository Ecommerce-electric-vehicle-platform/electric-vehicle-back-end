package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.enumerate.OrderStatus;
import Green_trade.green_trade_platform.exception.PaymentMethodNotSupportedException;
import Green_trade.green_trade_platform.exception.PostProductNotFound;
import Green_trade.green_trade_platform.exception.ProfileException;
import Green_trade.green_trade_platform.exception.SelfPurchaseNotAllowedException;
import Green_trade.green_trade_platform.mapper.BuyerMapper;
import Green_trade.green_trade_platform.mapper.OrderMapper;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.mapper.WalletMapper;
import Green_trade.green_trade_platform.model.*;
import Green_trade.green_trade_platform.repository.*;
import Green_trade.green_trade_platform.request.PlaceOrderRequest;
import Green_trade.green_trade_platform.request.ProfileRequest;
import Green_trade.green_trade_platform.request.UpdateBuyerProfileRequest;
import Green_trade.green_trade_platform.response.BuyerResponse;
import Green_trade.green_trade_platform.response.OrderResponse;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.service.implement.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/buyer")
@Slf4j
@AllArgsConstructor
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

    @Operation(
            summary = "Upload buyer profile",
            description = "Upload buyer profile: avatar, full name, shipping address, and so on"
    )
    @PostMapping(
            value = "/upload-profile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('ROLE_BUYER')")
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

    @Operation(summary = "Update Profile Buyer",
                description = "Update buyer profile: buyer profile information")
    @PutMapping(
            value = "/update-profile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
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
            summary = "Get buyer profile.",
            description = "This API return user profile. Front-end just pass token into header of request," +
                    " then system will return profile based on token passed."
    )
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

//    @PostMapping("/place-order")
//    public ResponseEntity<?> placeOrder(@Valid @RequestBody PlaceOrderRequest request) throws Exception {
//        Order newOrder = null;
//        RestResponse response = null;
//        OrderResponse responseData = null;
//        String shippingFee = "0";
//        Payment payment = paymentRepository.findById(request.getPaymentId()).orElseThrow(
//                () -> new Exception("Payment method is not existed")
//        );
//
//        Buyer buyer = buyerRepository.findByUsername(request.getUsername()).orElseThrow(() -> new ProfileNotFoundException("Buyer is not existed"));
//        PostProduct postProduct = postProductRepository.findById(request.getPostProductId()).orElseThrow(() -> new Exception("Post Product is not existed"));
//        if(payment.getGatewayName().equals("COD")) {
//            shippingFee = ghnService.getShippingFeeDto(buyer, postProduct.getSeller(), postProduct, postProduct.getPrice().intValue()).get("total");
//        } else {
//            shippingFee = ghnService.getShippingFeeDto(buyer, postProduct.getSeller(), postProduct, 0).get("total");
//        }
//        newOrder = buyerService.placeOrder(request, shippingFee);
//
//        if(payment.getGatewayName().equals("COD")) {
//            //đây là luồng xử lý cho thanh toán COD
//            Transaction transaction = transactionService.checkoutCODPayment(
//                    request.getUsername(),
//                    request.getPostProductId(),
//                    request.getPaymentId(),
//                    newOrder
//            );
//            List<Transaction> transactions = transactionService.getTransactionsOfOrder(newOrder);
//            newOrder = buyerService.updateOrderTransactions(newOrder, transactions);
//            responseData = orderMapper.toDto(newOrder);
//        } else {
//            //tạo transaction cho việc thanh toán
//            Transaction transaction = transactionService.checkoutWalletPayment(
//                    request.getUsername(),
//                    request.getPostProductId(),
//                    request.getPaymentId(),
//                    newOrder
//            );
//            List<Transaction> transactions = transactionService.getTransactionsOfOrder(newOrder);
//            newOrder = buyerService.updateOrderTransactions(newOrder, transactions);
//            newOrder = buyerService.updateOrderStatus(newOrder, "PAID");
//            responseData = orderMapper.toDto(newOrder);
//        }
//
//        response = responseMapper.toDto(
//                true,
//                "PLACE ORDERED SUCCESS",
//                responseData,
//                null
//        );
//
//        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
//    }

    @Operation(
            summary = "Place a new order",
            description = """
                This endpoint allows a buyer to place a new order in the Green Trade platform.
                <br><br>
                Workflow:
                <ul>
                    <li>Validate the payment method and buyer information.</li>
                    <li>Fetch the product and verify its availability.</li>
                    <li>Reject the request if the buyer attempts to purchase their own product.</li>
                    <li>Calculate the shipping fee through the GHN API (depending on COD or online payment).</li>
                    <li>Create a new order, transaction, and GHN shipping order.</li>
                    <li>Return a response containing the new order details and GHN order code.</li>
                </ul>
                """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The request body containing buyer, product, and payment information to place an order.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PlaceOrderRequest.class),
                            examples = @ExampleObject(
                                    name = "Example Request",
                                    value = """
                                        {
                                          "postProductId": 12,
                                          "username": "duyphuong123",
                                          "fullName": "Tên đầy đủ",
                                          "street": "256 Nguyễn Thị Minh Khai",
                                          "wardName": "Phường Bến Thành",
                                          "districtName": "Quận 1",
                                          "provinceName": "Hồ Chí Minh",
                                          "phoneNumber": "0905123456",
                                          "shippingPartnerId": 1,
                                          "paymentId": 1
                                        }
                                        """
                            )
                    )
            )
    )
    @PostMapping("/place-order")
    public ResponseEntity<RestResponse<OrderResponse, Object>> placeOrder(@Valid @RequestBody PlaceOrderRequest request) throws Exception {
        log.info(">>> [START] placeOrder");

        Order newOrder = null;
        RestResponse<OrderResponse, Object> response = null;
        OrderResponse responseData = null;
        String shippingFee = "0";

        log.info(">>> Fetch payment");
        Payment payment = paymentService.findPaymentMethodById(request.getPaymentId());
        if(payment == null) {
            throw new PaymentMethodNotSupportedException();
        }

        log.info(">>> Fetch buyer");
        Buyer buyer = buyerService.findBuyerByUsername(request.getUsername());
        if(buyer == null) {
            throw new ProfileException("Buyer with Username: " + request.getUsername() + "is not existed");
        }

        log.info(">>> Fetch post product");
        PostProduct postProduct = postProductService.findPostProductById(request.getPostProductId());
        if(postProduct == null) {
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
            log.info(">>> COD payment flow");
            Transaction transaction = transactionService.checkoutCODPayment(
                    request.getUsername(),
                    request.getPostProductId(),
                    request.getPaymentId(),
                    newOrder
            );

            List<Transaction> transactions = transactionService.getTransactionsOfOrder(newOrder);
            log.info(">>> Passed get transactions");

            newOrder = orderService.updateOrderTransactions(newOrder, transactions);
            log.info(">>> Passed update transactions");

            String orderShippingCode = ghnService.createOrderShippingResponseToDto(
                    newOrder, transactionRepository.findAllByOrder(newOrder).getLast().getPayment()).get("orderCode");
            log.info(">>> Passed get orderShippingCode");
            log.info(">>> orderShippingCode: {}", orderShippingCode);

            newOrder = orderService.updateOrderCode(orderShippingCode, newOrder);
            log.info(">>> Passed set Order Code");
        } else {
            log.info(">>> Wallet payment flow");
            Transaction transaction = transactionService.checkoutWalletPayment(
                    request.getUsername(),
                    request.getPostProductId(),
                    request.getPaymentId(),
                    newOrder
            );
            SystemWallet systemWallet = systemWalletService.createEscrowRecord(newOrder);
            List<Transaction> transactions = transactionService.getTransactionsOfOrder(newOrder);
            log.info(">>> Passed get transactions");

            newOrder = orderService.updateOrderTransactions(newOrder, transactions);
            log.info(">>> Passed update transactions for order");

            newOrder = orderService.updateOrderStatus(newOrder, OrderStatus.PAID);
            log.info(">>> Passed update order status");

            String orderShippingCode = ghnService.createOrderShippingResponseToDto(
                    newOrder, transactionRepository.findAllByOrder(newOrder).getLast().getPayment()).get("orderCode");
            log.info(">>> Passed get orderShippingCode: {}", orderShippingCode);

            newOrder = orderService.updateOrderCode(orderShippingCode, newOrder);
            log.info(">>> Passed set Order Code");
        }
        responseData = orderMapper.toDto(newOrder);
        log.info(">>> Passed created response");

        postProductService.updateSoldStatus(true, postProduct);

        log.info(">>> Build response");
        response = responseMapper.toDto(
                true,
                "PLACE ORDERED SUCCESS",
                responseData,
                null
        );

        log.info(">>> [END] placeOrder success");
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

}
