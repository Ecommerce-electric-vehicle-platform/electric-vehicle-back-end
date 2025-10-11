package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.service.implement.VnPayServiceImpl;
import Green_trade.green_trade_platform.util.VnPayUtils;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

//    @GetMapping("/pay")
//    public ResponseEntity<?> createPaymentUrl(HttpServletRequest request, @RequestParam long amount, @RequestParam Long buyerId) throws IOException {
//        Map<String, Object> paymentUrl = vnPayService.processCreatePaymentUrl(request, buyerId, amount);
//        return  ResponseEntity.ok(responseMapper.toDto(true, "Tạo liên kết thanh toán thành công.", paymentUrl, null));
//    }

    @PostMapping("/create-payment")
    public ResponseEntity<?> createPayment(HttpServletRequest request,@RequestParam long buyerId, @RequestParam long amount) {
        try {
            Map<String, Object> result = vnPayService.createPaymentUrl(request, buyerId, amount);
            return ResponseEntity.ok(responseMapper.toDto(true, "Tạo liên kết thanh toán thành công.", result.toString(), null));
        } catch (Exception e) {
            JsonObject error = new JsonObject();
            error.addProperty("code", "99");
            error.addProperty("message", "error: " + e.getMessage());
            return ResponseEntity.badRequest().body(responseMapper.toDto(false, "Không thể tạo link thanh toán.", error.toString(), e));
        }
    }

    @GetMapping("/return")
    public ResponseEntity<?> handleVnPayReturn(HttpServletRequest request) {
        return ResponseEntity.ok(vnPayService.processReturn(request));
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
