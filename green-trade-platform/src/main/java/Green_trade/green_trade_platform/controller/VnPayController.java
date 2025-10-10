package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.service.implement.VnPayServiceImpl;
import Green_trade.green_trade_platform.util.VnPayUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vnpay")
public class VnPayController {
    private final VnPayServiceImpl vnPayService;
    private final VnPayUtils vnPayUtils;
    private final ResponseMapper responseMapper;
    @Value(("${vnp_HashSecret}"))
    private String secretKey;
    @Value("${vnp_HashSecret}")
    private String vnpHashSecret;

    public VnPayController(VnPayServiceImpl vnPayService, VnPayUtils vnPayUtils , ResponseMapper responseMapper) {
        this.vnPayService = vnPayService;
        this.vnPayUtils = vnPayUtils;
        this.responseMapper = responseMapper;
    }

    // Tạo link thanh toán
    @GetMapping("/pay")
    public ResponseEntity<?> createPayment(@RequestParam int amount, @RequestParam Long buyerId) throws IOException {
        String paymentUrl = vnPayService.createPaymentUrl(buyerId, amount);
        return  ResponseEntity.ok(responseMapper.toDto(true, "CREATE PAYMENT URL SUCCESS.", paymentUrl, null));
    }

    // Callback xử lý khi thanh toán xong
    @GetMapping("/callback")
    public String callback(@RequestParam Map<String, String> params) {
        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef");
        String amount = params.get("vnp_Amount");

        if ("00".equals(responseCode)) {
            // Thành công: cập nhật số dư user tại đây
            return "✅ Thanh toán thành công! Mã giao dịch: " + txnRef +
                    ", Số tiền: " + (Integer.parseInt(amount) / 100) + " VND";
        } else {
            return "❌ Thanh toán thất bại. Mã giao dịch: " + txnRef +
                    ", Mã lỗi: " + responseCode;
        }
    }

    @GetMapping("/return")
    public ResponseEntity<?> returnUrl(@RequestParam Map<String, String> params) {
        String responseCode = params.get("vnp_ResponseCode");
        if ("00".equals(responseCode)) {
            return ResponseEntity.ok("Thanh toán thành công! Bạn có thể quay lại ví.");
        }
        return ResponseEntity.ok("Thanh toán thất bại hoặc bị hủy.");
    }

//    @GetMapping("/ipn")
//    public ResponseEntity<String> vnpayIpn(@RequestParam Map<String, String> params) {
//        try {
//            String result = vnPayService.handleIpnCallback(params);
//            return ResponseEntity.ok(result);
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body("Error: " + e.getMessage());
//        }
//    }

}
