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
import Green_trade.green_trade_platform.response.WalletTransactionResponse;
import Green_trade.green_trade_platform.service.implement.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    private final WalletServiceImpl walletService;

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
                """
    )
    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER')")
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
                    newOrder, transactionRepository.findAllByOrder(newOrder).getLast().getPayment()
            ).get("orderCode");
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
}
