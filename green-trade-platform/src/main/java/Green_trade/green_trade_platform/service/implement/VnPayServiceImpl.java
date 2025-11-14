package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.config.VnPayConfig;
import Green_trade.green_trade_platform.service.VnPayService;
import Green_trade.green_trade_platform.model.Buyer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class VnPayServiceImpl implements VnPayService {
    @Value(("${vnp_TmnCode}"))
    private String vnpTmnCode;
    @Value("${vnp_HashSecret}")
    private String vnpHashSecret;
    @Value(("${vnp_Url}"))
    private String vnpUrl;
    @Value(("${vnpay.return-url}"))
    private String vnpReturnUrl;

    private final BuyerServiceImpl buyerService;
    private final VnPayConfig vnPayConfig;
//    private final VnPayUtils vnPayUtils;
//    private final WalletRepository walletRepository;

    public VnPayServiceImpl(VnPayConfig config, BuyerServiceImpl buyerService) {
        this.vnPayConfig = config;
        this.buyerService = buyerService;
    }

    public Map<String, Object> createPaymentUrl(HttpServletRequest req, long amount) throws Exception {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TmnCode = VnPayConfig.vnp_TmnCode;
        String vnp_TxnRef = VnPayConfig.getRandomNumber(8);
        String vnp_IpAddr = req.getRemoteAddr();
        Buyer buyer = buyerService.getCurrentUser();

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
        vnp_Params.put("vnp_CurrCode", "VND");

        String bankCode = req.getParameter("bankcode");
        if (bankCode != null && !bankCode.isEmpty()) {
            vnp_Params.put("vnp_BankCode", bankCode);
        }

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", buyer.getBuyerId().toString() + " : " + buyer.getUsername() + " nạp tiền vào ví.");
        vnp_Params.put("vnp_OrderType", Optional.ofNullable(req.getParameter("ordertype")).orElse("other"));
        vnp_Params.put("vnp_Locale", Optional.ofNullable(req.getParameter("language")).orElse("vn"));
        // Sử dụng return URL từ application.properties, nếu không có thì dùng default từ VnPayConfig
        String returnUrl = vnpReturnUrl != null && !vnpReturnUrl.isEmpty() 
                ? vnpReturnUrl 
                : VnPayConfig.vnp_ReturnUrl;
        vnp_Params.put("vnp_ReturnUrl", returnUrl);
        log.info(">>> [VNPay] Using return URL: {}", returnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        // Sử dụng timezone Asia/Ho_Chi_Minh (UTC+7) - giờ Việt Nam
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        String vnp_CreateDate = formatter.format(cld.getTime());
        
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        
        // Cộng thêm 15 phút để tính expire
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Build data to hash and querystring (theo đúng code mẫu VNPay)
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                try {
                    // Build hash data: encode với US_ASCII (theo code mẫu VNPay)
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    
                    // Build query: encode với US_ASCII (theo code mẫu VNPay)
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                } catch (UnsupportedEncodingException e) {
                    log.error(">>> [VNPay] Error encoding field: {}", fieldName, e);
                    throw new RuntimeException("Error encoding VNPay parameters", e);
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = VnPayConfig.hmacSHA512(VnPayConfig.vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        
        String paymentUrl = VnPayConfig.vnp_Url + "?" + queryUrl;
        
        log.info(">>> [VNPay] Hash data (for hash calculation): {}", hashData.toString());
        log.info(">>> [VNPay] Secure hash: {}", vnp_SecureHash);
        
        log.info(">>> [VNPay] Payment URL created successfully");
        log.info(">>> [VNPay] Return URL: {}", returnUrl);
        log.info(">>> [VNPay] Payment URL length: {}", paymentUrl.length());
        log.debug(">>> [VNPay] Full payment URL: {}", paymentUrl);

        Map<String, Object> result = new HashMap<>();
        result.put("url_payment", paymentUrl);
        return result;
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
                result.put("response_code", responseCode);
            } else {
                result.put("success", false);
                result.put("", transactionCode);
                result.put("message", "Thanh toán thất bại!");
                result.put("response_code", responseCode);
            }
        } else {
            result.put("success", false);
            result.put("transaction_code", transactionCode);
            result.put("message", "Mã bảo mật không hợp lệ");
            result.put("vnp_secureHash", vnp_SecureHash);
            result.put("sign_value", secureHash);
            result.put("response_code", responseCode);
        }

        return result;
    }
}


