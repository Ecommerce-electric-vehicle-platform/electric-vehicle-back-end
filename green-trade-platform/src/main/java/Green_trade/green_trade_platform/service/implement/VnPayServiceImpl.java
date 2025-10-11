package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.config.VnPayConfig;
import Green_trade.green_trade_platform.repository.WalletRepository;
import Green_trade.green_trade_platform.util.VnPayUtils;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class VnPayServiceImpl {
    @Value(("${vnp_TmnCode}"))
    private String vnpTmnCode;
    @Value("${vnp_HashSecret}")
    private String vnpHashSecret;
    @Value(("${vnp_Url}"))
    private String vnpUrl;
    @Value(("${vnpay.return-url}"))
    private String vnpReturnUrl;
    private final VnPayConfig vnPayConfig;
//    private final VnPayUtils vnPayUtils;
//    private final WalletRepository walletRepository;

    public VnPayServiceImpl(VnPayConfig config) {
        this.vnPayConfig = config;
    }

    public Map<String, Object> processCreatePaymentUrl(HttpServletRequest request,long buyerId, long amount) {
        try {
            String vnp_TmnCode = VnPayConfig.vnp_TmnCode;
            String vnp_HashSecret = VnPayConfig.vnp_HashSecret;
            String vnp_Url = VnPayConfig.vnp_Url;
            String vnp_ReturnUrl = vnpReturnUrl;

            String vnp_TxnRef = String.valueOf(System.currentTimeMillis());
            String vnp_Amount = String.valueOf(amount * 100);
            String vnp_Locale = "vn";
            String vnp_BankCode = ""; // nếu có thì set vào sau
//            String vnp_IpAddr = request.getRemoteAddr();
            String vnp_IpAddr = "127.0.0.1";

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

// Lấy thời gian bắt đầu theo múi giờ Việt Nam
            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            String startTime = sdf.format(cld.getTime());

// Cộng thêm 15 phút để tính expire
            cld.add(Calendar.MINUTE, 15);
            String expire = sdf.format(cld.getTime());

            Map<String, String> inputData = new HashMap<>();
            inputData.put("vnp_Version", "2.1.0");
            inputData.put("vnp_TmnCode", vnp_TmnCode);
            inputData.put("vnp_Amount", vnp_Amount);
            inputData.put("vnp_Command", "pay");
            inputData.put("vnp_CreateDate", startTime);
            inputData.put("vnp_CurrCode", "VND");
            inputData.put("vnp_IpAddr", vnp_IpAddr);
            inputData.put("vnp_Locale", vnp_Locale);
            inputData.put("vnp_OrderInfo", "Thanh toan GD:" + vnp_TxnRef);
            inputData.put("vnp_OrderType", "billpayment");
            inputData.put("vnp_ReturnUrl", vnp_ReturnUrl);
            inputData.put("vnp_TxnRef", vnp_TxnRef);
            inputData.put("vnp_ExpireDate", expire);

            if (!vnp_BankCode.isEmpty()) {
                inputData.put("vnp_BankCode", "VNPAYQR");
            }

            // sort key
            List<String> fieldNames = new ArrayList<>(inputData.keySet());
            Collections.sort(fieldNames);

            // build hashData và query
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            boolean first = true;
            for (String key : fieldNames) {
                String value = inputData.get(key);
                if (value != null && !value.isEmpty()) {
                    if (!first) {
                        hashData.append("&");
                    }
                    hashData.append(URLEncoder.encode(key, StandardCharsets.UTF_8))
                            .append("=")
                            .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
                    query.append(URLEncoder.encode(key, StandardCharsets.UTF_8))
                            .append("=")
                            .append(URLEncoder.encode(value, StandardCharsets.UTF_8))
                            .append("&");
                    first = false;
                }
            }

            // tạo chữ ký HMAC SHA512
            String vnp_SecureHash = VnPayConfig.hmacSHA512(vnp_HashSecret, hashData.toString());

            // append vào query
            query.append("vnp_SecureHash=").append(vnp_SecureHash);

            String paymentUrl = vnp_Url + "?" + query.toString();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("url_payment", paymentUrl);
            result.put("message", "Tạo liên kết thanh toán thành công");

            log.info("VNPay URL: {}", paymentUrl);
            return result;

        } catch (Exception e) {
            log.error("Error creating VNPay invoice", e);
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> processReturn(HttpServletRequest request) {
        Map<String, String> inputData = new HashMap<>();

        // Lấy toàn bộ tham số vnp_ gửi về
        request.getParameterMap().forEach((key, value) -> {
            if (key.startsWith("vnp_")) {
                inputData.put(key, value[0]);
            }
        });

        String vnp_SecureHash = inputData.get("vnp_SecureHash");
        inputData.remove("vnp_SecureHash");
        inputData.remove("vnp_SecureHashType");

        // Sắp xếp theo thứ tự key tăng dần
        List<String> fieldNames = new ArrayList<>(inputData.keySet());
        Collections.sort(fieldNames);

        // Ghép chuỗi dữ liệu để hash
        StringBuilder hashData = new StringBuilder();
        for (int i = 0; i < fieldNames.size(); i++) {
            String key = fieldNames.get(i);
            String value = inputData.get(key);
            try {
                if (i > 0) {
                    hashData.append('&');
                }
                hashData.append(URLEncoder.encode(key, "US-ASCII"))
                        .append('=')
                        .append(URLEncoder.encode(value, "US-ASCII"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        // Hash chuỗi dữ liệu
        String secureHash = VnPayConfig.hmacSHA512(VnPayConfig.vnp_HashSecret, hashData.toString());

        String transactionCode = inputData.get("vnp_TxnRef");
        String responseCode = inputData.get("vnp_ResponseCode");

        Map<String, Object> result = new HashMap<>();

        if (secureHash.equals(vnp_SecureHash)) {
            if ("00".equals(responseCode)) {
                result.put("success", true);
                result.put("transaction_code", transactionCode);
                result.put("message", "Xác minh thành công");
            } else {
                result.put("success", false);
                result.put("transaction_code", transactionCode);
                result.put("message", "Thanh toán thất bại!");
            }
        } else {
            result.put("success", false);
            result.put("transaction_code", transactionCode);
            result.put("message", "Mã bảo mật không hợp lệ");
            result.put("vnp_secureHash", vnp_SecureHash);
            result.put("sign_value", secureHash);
        }

        return result;
    }
}


