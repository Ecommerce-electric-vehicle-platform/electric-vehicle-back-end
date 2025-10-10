package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.config.VnPayConfig;
import Green_trade.green_trade_platform.repository.WalletRepository;
import Green_trade.green_trade_platform.util.VnPayUtils;
import com.google.gson.JsonObject;
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

    public String createPaymentUrl(Long buyerId, int amount) throws UnsupportedEncodingException {
        String vnp_TxnRef = VnPayConfig.getRandomNumber(8); // Mã giao dịch
        String vnp_IpAddr = "127.0.0.1";
        String orderType = "other";

        String vnp_TmnCode = VnPayConfig.vnp_TmnCode;
        String vnp_Returnurl = VnPayConfig.vnp_ReturnUrl;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay yêu cầu nhân 100
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang buyerId=" + buyerId);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_Returnurl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        // Thời gian
        TimeZone tz = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        Calendar cld = Calendar.getInstance(tz);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(tz);
        String vnp_CreateDate = formatter.format(cld.getTime());
        cld.add(Calendar.MINUTE, 30);
        String vnp_ExpireDate = formatter.format(cld.getTime());


        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Build data string for hash
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName).append('=').append(fieldValue);
                query.append(URLEncoder.encode(fieldName, "UTF-8")).append('=')
                        .append(URLEncoder.encode(fieldValue, "UTF-8"));
                if (!fieldName.equals(fieldNames.get(fieldNames.size() - 1))) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }

        String vnp_SecureHash = VnPayConfig.hmacSHA512(VnPayConfig.vnp_HashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);

        return VnPayConfig.vnp_Url + "?" + query.toString();
    }
//    public VnPayServiceImpl(VnPayUtils vnPayUtils, WalletRepository walletRepository) {
//        this.vnPayUtils = vnPayUtils;
//        this.walletRepository = walletRepository;
//    }
//
//    public String createPaymentUrl(Long buyerId, int amount) {
//        try {
//            Map<String, String> vnp_Params = new HashMap<>();
//            vnp_Params.put("vnp_Version", "2.1.0");
//            vnp_Params.put("vnp_Command", "pay");
//            vnp_Params.put("vnp_TmnCode", vnpTmnCode);
//
//            // đảm bảo nhân 100 (amount là VND)
//            long vnpAmount = (long) amount * 100L;
//            vnp_Params.put("vnp_Amount", String.valueOf(vnpAmount));
//
//            vnp_Params.put("vnp_CurrCode", "VND");
//
//            // OrderInfo: không để ký tự lạ ở cuối, dùng khoảng trắng (sẽ được encode)
//            String orderInfo = "Nap tien vao vi buyer " + buyerId;
//            vnp_Params.put("vnp_OrderInfo", orderInfo);
//
//            vnp_Params.put("vnp_Locale", "vn");
//            vnp_Params.put("vnp_ReturnUrl", vnpReturnUrl);
//            vnp_Params.put("vnp_IpAddr", "127.0.0.1");
//
//            // Thời gian tạo
//            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
//            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
//            String vnp_CreateDate = formatter.format(cld.getTime());
//            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
//            cld.add(Calendar.MINUTE, 15);
//            vnp_Params.put("vnp_ExpireDate", formatter.format(cld.getTime()));
//
//            // TxnRef duy nhất
//            String txnRef = String.valueOf(System.currentTimeMillis());
//            vnp_Params.put("vnp_TxnRef", txnRef);
//
//            // Build query & hash
//            Map<String, String> data = vnPayUtils.buildQueryAndHashData(vnp_Params);
//            String hashData = data.get("hashData");
//            String query = data.get("query");
//
//            String vnp_SecureHash = VnPayUtils.hmacSHA512(vnpHashSecret, hashData);
//            vnp_SecureHash = vnp_SecureHash.trim().toLowerCase();
//
//            // debug logs (rất quan trọng khi test)
//            log.info("VNPay hashData   : {}", hashData);
//            log.info("VNPay secureHash : {}", vnp_SecureHash);
//            log.info("VNPay query      : {}", query);
//
//            // encode secureHash trước khi nối vào URL
//            String paymentUrl = vnpUrl + "?" + query + "&vnp_SecureHash=" + URLEncoder.encode(vnp_SecureHash, StandardCharsets.UTF_8);
//
//            log.info(">>> Payment URL: {}", paymentUrl);
//            return paymentUrl;
//
//        } catch (Exception e) {
//            throw new RuntimeException("Lỗi khi tạo link thanh toán VNPay: " + e.getMessage(), e);
//        }
//    }
//
//    public String handleIpnCallback(Map<String, String> params) {
//        try {
//            // 1️⃣ Lấy SecureHash gửi về
//            String receivedHash = params.get("vnp_SecureHash");
//            params.remove("vnp_SecureHash");
//            params.remove("vnp_SecureHashType");
////            String temp = params.get("vnp_OrderInfo");
////            Long userId = Long.parseLong(temp.split("_")[5]);
//
//            // 2️⃣ Tạo lại hash từ dữ liệu để xác minh
//            Map<String, String> data = vnPayUtils.buildQueryAndHashData(params);
//            String calculatedHash = VnPayUtils.hmacSHA512(vnpHashSecret, data.get("hashData"));
//
//            if (!calculatedHash.equals(receivedHash)) {
//                throw new IllegalArgumentException("Invalid signature");
//            }
//
//            // 3️⃣ Kiểm tra mã phản hồi
//            String responseCode = params.get("vnp_ResponseCode");
//            if (!"00".equals(responseCode)) {
//                return "FAILED";
//            }
//
//            // 4️⃣ Lấy dữ liệu cần thiết
//            String amountStr = params.get("vnp_Amount");
//            String txnRef = params.get("vnp_TxnRef");
//
//            long amount = Long.parseLong(amountStr) / 100; // vì VNPay nhân 100
//            log.info(">>> Giao dịch thành công - Mã: {}, Số tiền: {}", txnRef, amount);
//
////            // 5️⃣ (TODO) Cập nhật ví người dùng
////            walletRepository.addBalance(userId, amount);
//
//            return "OK";
//
//        } catch (Exception e) {
//            log.info(">>> Lỗi xử lý IPN: {}", e.getMessage());
//            throw new RuntimeException(e);
//        }
//    }
}
