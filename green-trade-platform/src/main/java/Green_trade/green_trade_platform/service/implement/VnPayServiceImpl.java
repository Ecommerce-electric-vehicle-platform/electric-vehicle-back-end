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

    //    public String createPaymentUrl(Long buyerId, int amount) throws UnsupportedEncodingException {
//        String vnp_TxnRef = VnPayConfig.getRandomNumber(8); // Mã giao dịch
//        String vnp_IpAddr = "127.0.0.1";
//        String orderType = "other";
//
//        String vnp_TmnCode = VnPayConfig.vnp_TmnCode;
//        String vnp_Returnurl = VnPayConfig.vnp_ReturnUrl;
//
//        Map<String, String> vnp_Params = new HashMap<>();
//        vnp_Params.put("vnp_Version", "2.1.0");
//        vnp_Params.put("vnp_Command", "pay");
//        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
//        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay yêu cầu nhân 100
//        vnp_Params.put("vnp_CurrCode", "VND");
//        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
//        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang buyerId=" + buyerId);
//        vnp_Params.put("vnp_OrderType", orderType);
//        vnp_Params.put("vnp_Locale", "vn");
//        vnp_Params.put("vnp_ReturnUrl", vnp_Returnurl);
//        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
//
//        // Thời gian
//
//        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
//        formatter.setTimeZone(cld.getTimeZone());
//        String vnp_CreateDate = formatter.format(cld.getTime());
//        cld.add(Calendar.MINUTE, 30);
//        String vnp_ExpireDate = formatter.format(cld.getTime());
//
//
//        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
//        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
//
//        // Build data string for hash
//        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
//        Collections.sort(fieldNames);
//        StringBuilder hashData = new StringBuilder();
//        StringBuilder query = new StringBuilder();
//
//        for (String fieldName : fieldNames) {
//            String fieldValue = vnp_Params.get(fieldName);
//            if ((fieldValue != null) && (fieldValue.length() > 0)) {
//                hashData.append(fieldName).append('=').append(fieldValue);
//                query.append(URLEncoder.encode(fieldName, "UTF-8")).append('=')
//                        .append(URLEncoder.encode(fieldValue, "UTF-8"));
//                if (!fieldName.equals(fieldNames.get(fieldNames.size() - 1))) {
//                    hashData.append('&');
//                    query.append('&');
//                }
//            }
//        }
//
//        String vnp_SecureHash = VnPayConfig.hmacSHA512(VnPayConfig.vnp_HashSecret, hashData.toString());
//        query.append("&vnp_SecureHash=").append(vnp_SecureHash);
//
//        return VnPayConfig.vnp_Url + "?" + query.toString();
//    }
    public Map<String, Object> createInvoiceVNPAY(HttpServletRequest request,long buyerId, long amount) {
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
            String startTime = sdf.format(new Date());
            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            cld.setTime(sdf.parse(startTime));
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
            inputData.put("vnp_OrderType", "other");
            inputData.put("vnp_ReturnUrl", vnp_ReturnUrl);
            inputData.put("vnp_TxnRef", vnp_TxnRef);
            inputData.put("vnp_ExpireDate", expire);

            if (!vnp_BankCode.isEmpty()) {
                inputData.put("vnp_BankCode", vnp_BankCode);
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
}


