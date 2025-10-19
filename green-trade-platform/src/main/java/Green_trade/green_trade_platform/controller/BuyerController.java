package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.BuyerMapper;
import Green_trade.green_trade_platform.mapper.OrderMapper;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.mapper.WalletMapper;
import Green_trade.green_trade_platform.model.*;
import Green_trade.green_trade_platform.repository.PaymentRepository;
import Green_trade.green_trade_platform.request.PlaceOrderRequest;
import Green_trade.green_trade_platform.request.ProfileRequest;
import Green_trade.green_trade_platform.request.UpdateBuyerProfileRequest;
import Green_trade.green_trade_platform.response.BuyerResponse;
import Green_trade.green_trade_platform.response.OrderResponse;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.service.TransactionService;
import Green_trade.green_trade_platform.service.implement.BuyerServiceImpl;
import Green_trade.green_trade_platform.service.implement.TransactionServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Operation(
            summary = "Upload buyer profile",
            description = "Upload buyer profile: avatar, full name, shipping address, and so on"
    )
    @PostMapping(
            value = "/{id}/upload-profile",
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
            value = "/{id}/update-profile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<RestResponse<BuyerResponse, Object>> updateProfile(
            @Valid @ModelAttribute UpdateBuyerProfileRequest updateProfileRequest,
            @RequestPart(value = "avatarImage") MultipartFile avatarFile
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
    public ResponseEntity<?> getProfile() {
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
    public ResponseEntity<?> getWallet() {
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

    @PostMapping("/place-order")
    public ResponseEntity<?> placeOrder(@Valid PlaceOrderRequest request) throws Exception {
        Order newOrder = null;
        RestResponse response = null;
        OrderResponse responseData = null;
        Payment payment = paymentRepository.findById(request.getPaymentId()).orElseThrow(
                () -> new Exception("Payment method is not existed")
        );

        newOrder = buyerService.placeOrder(request);

        if(payment.getGatewayName().equals("COD")) {
            //đây là luồng xử lý cho thanh toán COD
            Transaction transaction = transactionService.checkoutCODPayment(
                    request.getUsername(),
                    request.getPostProductId(),
                    request.getPaymentId(),
                    newOrder
            );
            List<Transaction> transactions = transactionService.getTransactionsOfOrder(newOrder);
            newOrder = buyerService.updateOrderTransactions(newOrder, transactions);
            responseData = orderMapper.toDto(newOrder);
        } else {
            //tạo transaction cho việc thanh toán
            Transaction transaction = transactionService.checkoutWalletPayment(
                    request.getUsername(),
                    request.getPostProductId(),
                    request.getPaymentId(),
                    newOrder
            );
            List<Transaction> transactions = transactionService.getTransactionsOfOrder(newOrder);
            newOrder = buyerService.updateOrderTransactions(newOrder, transactions);
            newOrder = buyerService.updateOrderStatus(newOrder, "PAID");
            responseData = orderMapper.toDto(newOrder);
        }

        response = responseMapper.toDto(
                true,
                "PLACE ORDERED SUCCESS",
                responseData,
                null
        );

        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }
}
