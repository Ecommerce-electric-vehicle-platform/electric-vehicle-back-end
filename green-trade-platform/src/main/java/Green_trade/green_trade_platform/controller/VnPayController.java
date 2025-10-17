package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.mapper.WalletMapper;
import Green_trade.green_trade_platform.model.Wallet;
import Green_trade.green_trade_platform.service.implement.VnPayServiceImpl;
import Green_trade.green_trade_platform.service.implement.WalletServiceImpl;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vnpay")
@Slf4j
public class VnPayController {
    @Value(("${vnp_HashSecret}"))
    private String secretKey;
    @Value("${vnp_HashSecret}")
    private String vnpHashSecret;

    private final VnPayServiceImpl vnPayService;
    private final ResponseMapper responseMapper;
    private final WalletServiceImpl walletServiceImpl;
    private final WalletMapper walletMapper;

    public VnPayController(VnPayServiceImpl vnPayService,
                           ResponseMapper responseMapper,
                           WalletServiceImpl walletServiceImpl,
                           WalletMapper walletMapper) {
        this.vnPayService = vnPayService;
        this.responseMapper = responseMapper;
        this.walletServiceImpl = walletServiceImpl;
        this.walletMapper = walletMapper;
    }

    @PostMapping("/create-payment")
    public ResponseEntity<?> createPayment(HttpServletRequest request, @RequestParam long amount) {
        try {
            Map<String, Object> result = vnPayService.createPaymentUrl(request, amount);
            return ResponseEntity.ok(
                    responseMapper.toDto(true,
                    "Tạo liên kết thanh toán thành công.",
                    result,
                    null));
        } catch (Exception e) {
            JsonObject error = new JsonObject();
            error.addProperty("code", "99");
            error.addProperty("message", "error: " + e.getMessage());
            return ResponseEntity.badRequest().body(
                    responseMapper.toDto(false,
                            "Không thể tạo link thanh toán.",
                            error,
                            e));
        }
    }

    @GetMapping("/return")
    public ResponseEntity<?> handleVnPayReturn(HttpServletRequest request) {
        Map<String, Object> result = vnPayService.processReturn(request);
        if(result.get("response_code").equals("00")) {
//            Map<String, String> inputData = new HashMap<>();

//            // Get all parameters of VNPay response
//            request.getParameterMap().forEach((key, value) -> {
//                if (key.startsWith("vnp_")) {
//                    inputData.put(key, value[0]);
//                }
//            });
            return ResponseEntity.ok(responseMapper.toDto(
                    false, "Nạp tiền thành công.",
                    result.get("response_code"), null));
        }
        return ResponseEntity.ok(responseMapper.toDto(
                false, "Nạp tiền không thành công.",
                result.get("response_code"), null));
    }

    @GetMapping("/ipn")
    public ResponseEntity<?> ipn(HttpServletRequest request) {
        log.info(">>> Chạy vào ipn rồi.");
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

}
