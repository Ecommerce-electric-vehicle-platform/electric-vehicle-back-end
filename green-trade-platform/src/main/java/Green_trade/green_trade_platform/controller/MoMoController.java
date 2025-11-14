package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.mapper.WalletMapper;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.Wallet;
import Green_trade.green_trade_platform.service.implement.BuyerServiceImpl;
import Green_trade.green_trade_platform.service.implement.MoMoServiceImpl;
import Green_trade.green_trade_platform.service.implement.WalletServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import Green_trade.green_trade_platform.util.DateUtils;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/momo")
@Slf4j
public class MoMoController {
    private final MoMoServiceImpl moMoService;
    private final ResponseMapper responseMapper;
    private final WalletServiceImpl walletServiceImpl;
    private final WalletMapper walletMapper;
    private final BuyerServiceImpl buyerService;

    public MoMoController(MoMoServiceImpl moMoService,
                          ResponseMapper responseMapper,
                          WalletServiceImpl walletServiceImpl,
                          WalletMapper walletMapper,
                          BuyerServiceImpl buyerService) {
        this.moMoService = moMoService;
        this.responseMapper = responseMapper;
        this.walletMapper = walletMapper;
        this.walletServiceImpl = walletServiceImpl;
        this.buyerService = buyerService;
    }

    @Operation(
            summary = "Create MoMo payment link",
            description = """
                    Creates a MoMo payment URL for users to initiate a payment process.  
                    The system communicates with the MoMo API, generates a secure payment URL, 
                    and returns it to the client. The user can then be redirected to this URL to complete the transaction.
                
                    **Workflow:**
                    1. The client sends a request with the desired payment `amount`.
                    2. The server constructs a payment request containing order details, amount, timestamp, 
                       and the client's information.
                    3. The request is signed using MoMo's HMAC SHA256 algorithm.
                    4. A MoMo payment URL is generated and returned to the frontend.
                    5. The user is redirected to MoMo to complete the transaction.
                
                    **Use cases:**
                    - Wallet top-up for buyers or sellers.
                    - Paying for premium packages or transactions through MoMo.
                    - Integrating MoMo checkout functionality into your e-commerce flow.
                
                    **Security Notes:**
                    - The endpoint should be protected if it's part of an authenticated payment workflow.
                    - The `amount` should be validated server-side to prevent tampering.
                """
    )
    @PreAuthorize("hasAnyRole('ROLE_BUYER', 'ROLE_SELLER')")
    @PostMapping("/create-payment")
    public ResponseEntity<?> createPayment(HttpServletRequest request, @RequestParam long amount) {
        try {
            Map<String, Object> result = moMoService.createPaymentUrl(request, amount);
            return ResponseEntity.ok(
                    responseMapper.toDto(true,
                            "Tạo liên kết thanh toán MoMo thành công.",
                            result,
                            null));
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("code", "99");
            error.put("message", "error: " + e.getMessage());
            return ResponseEntity.badRequest().body(
                    responseMapper.toDto(false,
                            "Không thể tạo link thanh toán MoMo.",
                            error,
                            e));
        }
    }

    @Operation(
            summary = "Handle MoMo payment return callback",
            description = """
                    Handles the return callback from MoMo after a user completes or cancels a payment.  
                    This endpoint is invoked by MoMo once the transaction process finishes, providing details such as 
                    transaction status, order information, and payment amount.
                
                    **Workflow:**
                    1. MoMo redirects the user to this endpoint after payment completion.
                    2. The system validates the MoMo return parameters (signature, response code, etc.).
                    3. If `resultCode = "0"`, the payment is successful:
                       - The corresponding wallet is credited with the transaction amount.
                       - Transaction details are saved in the system.
                    4. If the result code is not `"0"`, the payment failed or was canceled.
                    5. A message indicating the payment result is returned to the frontend.
                
                    **Use cases:**
                    - Processing wallet top-up success or failure.
                    - Confirming order payment results from MoMo.
                    - Automatically updating wallet balance and transaction logs.
                
                    **Security Notes:**
                    - This endpoint is typically accessed by MoMo's redirect (user + MoMo callback).
                    - Request validation (signature, hash integrity) should be implemented server-side.
                """
    )
    @GetMapping("/return")
    public ResponseEntity<?> handleMoMoReturn(HttpServletRequest request) {
        try {
            log.info(">>> [MoMo Return] New request received at {}", DateUtils.getCurrentVietnamTime());
            
            // Log tất cả parameters từ MoMo để debug
            Map<String, String[]> paramMap = request.getParameterMap();
            log.info(">>> [MoMo Return] All parameters: {}", paramMap);
            paramMap.forEach((key, value) -> {
                log.info(">>> [MoMo Return] {} = {}", key, value != null && value.length > 0 ? value[0] : "null");
            });
            
            Map<String, Object> result = moMoService.processReturn(request);
            log.info(">>> [MoMo Return] Process result: {}", result);
            
            // Lấy resultCode từ result
            Object resultCodeObj = result.get("response_code");
            String resultCode = resultCodeObj != null ? resultCodeObj.toString() : "";
            
            log.info(">>> [MoMo Return] Result code: {}", resultCode);
            
            if ("0".equals(resultCode)) {
                // Thành công - xử lý nạp tiền vào ví
                try {
                    Map<String, String> inputData = new HashMap<>();
                    
                    // Lấy toàn bộ tham số từ MoMo gửi về
                    request.getParameterMap().forEach((key, value) -> {
                        if (value != null && value.length > 0) {
                            inputData.put(key, value[0]);
                        }
                    });
                    
                    log.info(">>> [MoMo Return] Input data for wallet deposit: {}", inputData);
                    
                    Wallet wallet = walletServiceImpl.processDepositMoneyFromMoMo(inputData);
                    log.info(">>> [MoMo Return] Wallet deposit successful: {}", wallet.getWalletId());
                    
                    return ResponseEntity.ok(responseMapper.toDto(
                            true, "Nạp tiền thành công.",
                            walletMapper.toDto(wallet), null));
                } catch (Exception e) {
                    log.error(">>> [MoMo Return] Error processing wallet deposit: {}", e.getMessage(), e);
                    return ResponseEntity.ok(responseMapper.toDto(
                            false, "Lỗi xử lý nạp tiền: " + e.getMessage(),
                            result, null));
                }
            }
            
            // Thất bại hoặc hủy
            String errorMessage = result.containsKey("message") 
                ? result.get("message").toString() 
                : "Nạp tiền không thành công.";
            
            return ResponseEntity.ok(responseMapper.toDto(
                    false, errorMessage,
                    result, null));
                    
        } catch (Exception e) {
            log.error(">>> [MoMo Return] Unexpected error: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("errorType", e.getClass().getSimpleName());
            return ResponseEntity.ok(responseMapper.toDto(
                    false, "Lỗi xử lý callback từ MoMo: " + e.getMessage(),
                    error, null));
        }
    }

    @Operation(
            summary = "Handle MoMo Instant Payment Notification (IPN)",
            description = """
                    Handles MoMo's Instant Payment Notification (IPN) — a **server-to-server** callback sent by MoMo to confirm the final status of a payment.  
                    This endpoint is called automatically by MoMo's system, even if the user closes the browser before returning to the site.
                
                    **Workflow:**
                    1. MoMo sends a POST request with multiple parameters (`amount`, `orderId`, `resultCode`, `signature`, etc.).
                    2. The system verifies the integrity of the data and validates the `signature`.
                    3. If `resultCode = "0"`, the transaction is successful:
                       - The corresponding wallet is credited with the deposited amount.
                       - Transaction details are logged in the database.
                    4. If the result code is not `"0"`, the transaction is considered failed or canceled.
                    5. The system responds to MoMo with an acknowledgment of the processing result.
                
                    **Difference between `/return` and `/ipn`:**
                    - `/return` → Called via user redirection after payment. Used for **frontend acknowledgment**.
                    - `/ipn` → Called automatically by MoMo (server-to-server). Used for **final confirmation**.
                
                    **Use cases:**
                    - Synchronizing payment success/failure status in the backend.
                    - Crediting wallet balances reliably, even if user doesn't return to the site.
                    - Ensuring payment data integrity between MoMo and internal systems.
                
                    **Security Notes:**
                    - Public endpoint, but requires signature validation (`signature`) for authentication.
                    - Idempotent processing is strongly recommended to prevent double deposits.
                """
    )
    @PostMapping("/ipn")
    public ResponseEntity<?> ipn(HttpServletRequest request) {
        log.info(">>> [MoMo IPN] New IPN request received at {}", DateUtils.getCurrentVietnamTime());
        Map<String, Object> result = moMoService.processReturn(request);
        
        // Lấy resultCode từ result
        Object resultCodeObj = result.get("response_code");
        String resultCode = resultCodeObj != null ? resultCodeObj.toString() : "";
        
        if ("0".equals(resultCode)) {
            // Thành công - xử lý nạp tiền vào ví
            Map<String, String> inputData = new HashMap<>();
            
            // Lấy toàn bộ tham số từ MoMo gửi về
            request.getParameterMap().forEach((key, value) -> {
                inputData.put(key, value[0]);
            });
            
            Wallet wallet = walletServiceImpl.processDepositMoneyFromMoMo(inputData);
            log.info(">>> [MoMo IPN] Wallet updated successfully: {}", wallet.getWalletId());
            
            return ResponseEntity.ok(responseMapper.toDto(
                    true, "Nạp tiền thành công.",
                    walletMapper.toDto(wallet), null));
        }
        
        return ResponseEntity.ok(responseMapper.toDto(
                false, "Nạp tiền không thành công.",
                result.get("response_code"), null));
    }
}

