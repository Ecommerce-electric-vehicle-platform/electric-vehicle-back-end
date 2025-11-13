package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.mapper.WalletMapper;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.Wallet;
import Green_trade.green_trade_platform.service.implement.BuyerServiceImpl;
import Green_trade.green_trade_platform.service.implement.VnPayServiceImpl;
import Green_trade.green_trade_platform.service.implement.WalletServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vnpay")
@Slf4j
@Tag(name = "VNPay Payment", description = "APIs for VNPay payment integration including wallet top-up and withdrawal")
public class VnPayController {
    @Value(("${vnp_HashSecret}"))
    private String secretKey;
    @Value("${vnp_HashSecret}")
    private String vnpHashSecret;

    private final VnPayServiceImpl vnPayService;
    private final ResponseMapper responseMapper;
    private final WalletServiceImpl walletServiceImpl;
    private final WalletMapper walletMapper;
    private final BuyerServiceImpl buyerService;

    public VnPayController(VnPayServiceImpl vnPayService,
                           ResponseMapper responseMapper,
                           WalletServiceImpl walletServiceImpl,
                           WalletMapper walletMapper,
                           BuyerServiceImpl buyerService) {
        this.vnPayService = vnPayService;
        this.responseMapper = responseMapper;
        this.walletMapper = walletMapper;
        this.walletServiceImpl = walletServiceImpl;
        this.buyerService = buyerService;
    }

    @Operation(
            summary = "Create VNPay payment link",
            description = """
                        Creates a VNPay payment URL for users to initiate a payment process.  
                        The system communicates with the VNPay API, generates a secure payment URL, 
                        and returns it to the client. The user can then be redirected to this URL to complete the transaction.
                    
                        **Workflow:**
                        1. The client sends a request with the desired payment `amount`.
                        2. The server constructs a payment request containing order details, amount, timestamp, 
                           and the client's IP address (extracted from the `HttpServletRequest`).
                        3. The request is signed using VNPay's secure hash algorithm (SHA256 or HMAC-SHA512).
                        4. A VNPay payment URL is generated and returned to the frontend.
                        5. The user is redirected to VNPay to complete the transaction.
                    
                        **Use cases:**
                        - Wallet top-up for buyers or sellers.
                        - Paying for premium packages or transactions through VNPay.
                        - Integrating VNPay checkout functionality into your e-commerce flow.
                    
                        **Security Notes:**
                        - The endpoint should be protected if it's part of an authenticated payment workflow.
                        - The `amount` should be validated server-side to prevent tampering.
                    """,
            tags = {"VNPay Payment"},
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment URL created successfully",
                    content = @Content(
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "Tạo liên kết thanh toán thành công.",
                                              "data": {
                                                "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=1000000&vnp_Command=pay&..."
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid amount or payment creation failed",
                    content = @Content(
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": false,
                                              "message": "Không thể tạo link thanh toán.",
                                              "data": {
                                                "code": "99",
                                                "message": "error: Invalid amount"
                                              },
                                              "error": "Invalid amount"
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
    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER')")
    @PostMapping("/create-payment")
    public ResponseEntity<?> createPayment(
            HttpServletRequest request,
            @Parameter(
                    description = "Payment amount in VND (must be positive)",
                    required = true,
                    example = "1000000"
            )
            @RequestParam long amount) {
        try {
            Map<String, Object> result = vnPayService.createPaymentUrl(request, amount);
            return ResponseEntity.ok(
                    responseMapper.toDto(true,
                            "Tạo liên kết thanh toán thành công.",
                            result,
                            null));
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", "99");
            error.put("message", "error: " + e.getMessage());
            return ResponseEntity.badRequest().body(
                    responseMapper.toDto(false,
                            "Không thể tạo link thanh toán.",
                            error,
                            e));
        }
    }

    @Operation(
            summary = "Handle VNPay payment return callback",
            description = """
                        Handles the return callback from VNPay after a user completes or cancels a payment.  
                        This endpoint is invoked by VNPay once the transaction process finishes, providing details such as 
                        transaction status, order information, and payment amount.
                    
                        **Workflow:**
                        1. VNPay redirects the user to this endpoint after payment completion.
                        2. The system validates the VNPay return parameters (signature, response code, etc.).
                        3. If `vnp_ResponseCode = "00"`, the payment is successful:
                           - The corresponding wallet is credited with the transaction amount.
                           - Transaction details are saved in the system.
                        4. If the response code is not `"00"`, the payment failed or was canceled.
                        5. A message indicating the payment result is returned to the frontend.
                    
                        **Use cases:**
                        - Processing wallet top-up success or failure.
                        - Confirming order payment results from VNPay.
                        - Automatically updating wallet balance and transaction logs.
                    
                        **Security Notes:**
                        - This endpoint is typically accessed by VNPay's redirect (user + VNPay callback).
                        - Request validation (signature, hash integrity) should be implemented server-side.
                        - Public endpoint - No authentication required (called by VNPay)
                    """,
            tags = {"VNPay Payment"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment processed successfully",
                    content = @Content(
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "Nạp tiền thành công.",
                                              "data": {
                                                "walletId": 1,
                                                "balance": 1000000.00,
                                                "updatedAt": "2024-01-15T10:30:00"
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment failed or canceled",
                    content = @Content(
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "Failed Response",
                                    value = """
                                            {
                                              "success": false,
                                              "message": "Nạp tiền không thành công.",
                                              "data": "07",
                                              "error": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/return")
    public ResponseEntity<?> handleVnPayReturn(HttpServletRequest request) {
        log.info(">>> [VNPay Return] New request received at {}", new Date());
        log.info(">>> [VNPay param]: {}", request.toString());
        Map<String, Object> result = vnPayService.processReturn(request);
        if (result.get("response_code").equals("00")) {
            Map<String, String> inputData = new HashMap<>();

            // Lấy toàn bộ tham số vnp_ gửi về
            request.getParameterMap().forEach((key, value) -> {
                if (key.startsWith("vnp_")) {
                    inputData.put(key, value[0]);
                }
            });

            Wallet wallet = walletServiceImpl.processDepositMoneyIntoWallet(inputData);
            return ResponseEntity.ok(responseMapper.toDto(
                    true, "Nạp tiền thành công.",
                    walletMapper.toDto(wallet), null));
        }
        return ResponseEntity.ok(responseMapper.toDto(
                false, "Nạp tiền không thành công.",
                result.get("response_code"), null));
    }


    //    Nếu deploy được thì lấy code này để làm
    @Operation(
            summary = "Handle VNPay Instant Payment Notification (IPN)",
            description = """
                        Handles VNPay's Instant Payment Notification (IPN) — a **server-to-server** callback sent by VNPay to confirm the final status of a payment.  
                        This endpoint is called automatically by VNPay's system, even if the user closes the browser before returning to the site.
                    
                        **Workflow:**
                        1. VNPay sends a GET request with multiple parameters (`vnp_Amount`, `vnp_TxnRef`, `vnp_ResponseCode`, `vnp_SecureHash`, etc.).
                        2. The system verifies the integrity of the data and validates the `vnp_SecureHash` signature.
                        3. If `vnp_ResponseCode = "00"`, the transaction is successful:
                           - The corresponding wallet is credited with the deposited amount.
                           - Transaction details are logged in the database.
                        4. If the response code is not `"00"`, the transaction is considered failed or canceled.
                        5. The system responds to VNPay with an acknowledgment of the processing result.
                    
                        **Difference between `/ipn` and `/return`:**
                        - `/ipn` → Called automatically by VNPay (server-to-server). Used for **final confirmation**.
                        - `/return` → Called via user redirection after payment. Used for **frontend acknowledgment**.
                    
                        **Use cases:**
                        - Synchronizing payment success/failure status in the backend.
                        - Crediting wallet balances reliably, even if user doesn't return to the site.
                        - Ensuring payment data integrity between VNPay and internal systems.
                    
                        **Security Notes:**
                        - Public endpoint, but requires signature validation (`vnp_SecureHash`) for authentication.
                        - Idempotent processing is strongly recommended to prevent double deposits.
                    """,
            tags = {"VNPay Payment"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "IPN processed successfully",
                    content = @Content(
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "Nạp tiền thành công.",
                                              "data": {
                                                "walletId": 1,
                                                "balance": 1000000.00
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "IPN payment failed",
                    content = @Content(
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": false,
                                              "message": "Nạp tiền không thành công.",
                                              "data": "07",
                                              "error": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/ipn")
    public ResponseEntity<?> ipn(HttpServletRequest request) {
        Map<String, Object> result = vnPayService.processReturn(request);
        if (result.get("response_code").equals("00")) {
            Map<String, String> inputData = new HashMap<>();

            // Lấy toàn bộ tham số vnp_ gửi về
            request.getParameterMap().forEach((key, value) -> {
                if (key.startsWith("vnp_")) {
                    inputData.put(key, value[0]);
                }
            });

            Wallet wallet = walletServiceImpl.processDepositMoneyIntoWallet(inputData);
            return ResponseEntity.ok(responseMapper.toDto(
                    true, "Nạp tiền thành công.",
                    walletMapper.toDto(wallet), null));
        }
        return ResponseEntity.ok(responseMapper.toDto(
                false, "Nạp tiền không thành công.",
                result.get("response_code"), null));
    }

    @Operation(
            summary = "Withdraw money from wallet",
            description = """
                        Allows an authenticated user (buyer or seller) to withdraw money from their wallet.
                        The system validates the withdrawal amount, checks wallet balance, and processes the withdrawal.
                    
                        **Workflow:**
                        1. User sends withdrawal request with desired amount
                        2. System validates:
                           - User is authenticated
                           - Wallet balance is sufficient
                           - Withdrawal amount is positive and within limits
                        3. System deducts amount from wallet
                        4. Returns updated wallet information
                    
                        **Use cases:**
                        - Users withdrawing funds to their bank account
                        - Sellers cashing out their earnings
                        - Buyers withdrawing unused wallet balance
                    
                        **Security Notes:**
                        - Requires authentication (ROLE_BUYER or ROLE_SELLER)
                        - Only wallet owner can withdraw from their own wallet
                        - Amount validation prevents negative or excessive withdrawals
                    """,
            tags = {"VNPay Payment"},
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Withdrawal processed successfully",
                    content = @Content(
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "Success Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "WITHDRAW MONEY SUCCESSFULLY.",
                                              "data": {
                                                "walletId": 1,
                                                "balance": 500000.00,
                                                "updatedAt": "2024-01-15T11:00:00"
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "Withdrawal failed",
                    content = @Content(
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    name = "Failed Response",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "WITHDRAW MONEY FAILED.",
                                              "data": null,
                                              "error": "Insufficient balance"
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
                    responseCode = "400",
                    description = "Bad Request - Invalid withdrawal amount"
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER')")
    @GetMapping("/with-draw")
    public ResponseEntity<?> withDrawMoney(
            @Parameter(
                    description = "Amount to withdraw in VND (must be positive and not exceed wallet balance)",
                    required = true,
                    example = "500000"
            )
            @RequestParam(name = "money") double money) {
        try {
            Buyer buyer = buyerService.getCurrentUser();
            Wallet wallet = walletServiceImpl.withDrawMoney(buyer, money);
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "WITHDRAW MONEY SUCCESSFULLY.",
                    walletMapper.toDto(wallet), null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "WITHDRAW MONEY FAILED.",
                    null, e.getMessage()
            ));
        }
    }

}
