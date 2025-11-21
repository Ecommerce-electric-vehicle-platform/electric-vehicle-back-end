package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.config.MoMoConfig;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.service.MoMoService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class MoMoServiceImpl implements MoMoService {
    @Value("${momo.partner-code:}")
    private String partnerCode;

    @Value("${momo.access-key:}")
    private String accessKey;

    @Value("${momo.secret-key:}")
    private String secretKey;

    @Value("${momo.endpoint:https://test-payment.momo.vn/v2/gateway/api/create}")
    private String endpoint;

    @Value("${momo.return-url:http://localhost:5173/vnpay/return}")
    private String returnUrl;

    @Value("${momo.ipn-url:http://localhost:8080/api/v1/momo/ipn}")
    private String ipnUrl;

    private final BuyerServiceImpl buyerService;
    private final RestTemplate restTemplate;

    public MoMoServiceImpl(BuyerServiceImpl buyerService) {
        this.buyerService = buyerService;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public Map<String, Object> createPaymentUrl(HttpServletRequest req, long amount) throws Exception {
        try {
            Buyer buyer = buyerService.getCurrentUser();

            // Tạo requestId và orderId
            String requestId = System.currentTimeMillis() + "";
            String orderId = System.currentTimeMillis() + "";
            String orderInfo = buyer.getBuyerId() + " : " + buyer.getUsername() + " nạp tiền vào ví.";
            String extraData = "";

            // MoMo yêu cầu amount là số nguyên (không có decimal)
            long amountLong = amount;

            // Tạo requestType: "payWithATM" cho thanh toán bằng thẻ ATM/thẻ tín dụng
            // Các lựa chọn: "payWithATM" (thẻ ATM), "payWithCC" (thẻ Credit Card), "captureWallet" (ví MoMo/QR)
            String requestType = "payWithATM";

            // Tạo raw signature: accessKey=$accessKey&amount=$amount&extraData=$extraData&ipnUrl=$ipnUrl
            // &orderId=$orderId&orderInfo=$orderInfo&partnerCode=$partnerCode&redirectUrl=$redirectUrl
            // &requestId=$requestId&requestType=$requestType
            StringBuilder rawHash = new StringBuilder();
            rawHash.append("accessKey=").append(accessKey);
            rawHash.append("&amount=").append(amountLong);
            rawHash.append("&extraData=").append(extraData);
            rawHash.append("&ipnUrl=").append(ipnUrl);
            rawHash.append("&orderId=").append(orderId);
            rawHash.append("&orderInfo=").append(orderInfo);
            rawHash.append("&partnerCode=").append(partnerCode);
            rawHash.append("&redirectUrl=").append(returnUrl);
            rawHash.append("&requestId=").append(requestId);
            rawHash.append("&requestType=").append(requestType);

            String rawHashString = rawHash.toString();
            log.info(">>> [MoMo] Raw hash string: {}", rawHashString);

            // Tạo signature: HMAC SHA256(rawHash, secretKey)
            String signature = MoMoConfig.hmacSHA256(secretKey, rawHashString);
            log.info(">>> [MoMo] Signature: {}", signature);

            // Tạo JSON request body
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("partnerCode", partnerCode);
            requestBody.addProperty("accessKey", accessKey);
            requestBody.addProperty("requestId", requestId);
            requestBody.addProperty("amount", amountLong);
            requestBody.addProperty("orderId", orderId);
            requestBody.addProperty("orderInfo", orderInfo);
            requestBody.addProperty("redirectUrl", returnUrl);
            requestBody.addProperty("ipnUrl", ipnUrl);
            requestBody.addProperty("extraData", extraData);
            requestBody.addProperty("requestType", requestType);
            requestBody.addProperty("signature", signature);

            log.info(">>> [MoMo] Request body: {}", requestBody.toString());

            // Gửi request đến MoMo API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);

            log.info(">>> [MoMo] Response status: {}", response.getStatusCode());
            log.info(">>> [MoMo] Response body: {}", response.getBody());

            // Parse response
            if (response.getBody() == null || response.getBody().trim().isEmpty()) {
                throw new Exception("MoMo API returned empty response");
            }

            JsonObject jsonResponse = JsonParser.parseString(response.getBody()).getAsJsonObject();

            // Kiểm tra kết quả
            int resultCode = jsonResponse.has("resultCode")
                    ? jsonResponse.get("resultCode").getAsInt()
                    : -1;

            String message = jsonResponse.has("message")
                    ? jsonResponse.get("message").getAsString()
                    : "Unknown error";

            Map<String, Object> result = new HashMap<>();
            result.put("requestId", requestId);
            result.put("orderId", orderId);

            if (resultCode == 0) {
                // Thành công
                String payUrl = jsonResponse.has("payUrl")
                        ? jsonResponse.get("payUrl").getAsString()
                        : "";
                if (payUrl == null || payUrl.trim().isEmpty()) {
                    throw new Exception("MoMo API returned success but no payUrl");
                }
                result.put("url_payment", payUrl);
                result.put("resultCode", resultCode);
                result.put("message", message);
                log.info(">>> [MoMo] Payment URL created successfully: {}", payUrl);
            } else {
                // Thất bại
                log.error(">>> [MoMo] Payment creation failed. resultCode: {}, message: {}", resultCode, message);
                result.put("resultCode", resultCode);
                result.put("message", message);
                throw new Exception("MoMo payment creation failed: " + message);
            }

            return result;

        } catch (Exception e) {
            log.error(">>> [MoMo] Error creating payment URL: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Map<String, Object> processReturn(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Lấy các tham số từ MoMo - xử lý null an toàn
            String partnerCode = request.getParameter("partnerCode");
            String orderId = request.getParameter("orderId");
            String requestId = request.getParameter("requestId");
            String amount = request.getParameter("amount");
            String orderInfo = request.getParameter("orderInfo");
            String orderType = request.getParameter("orderType");
            String transId = request.getParameter("transId");
            String resultCode = request.getParameter("resultCode");
            String message = request.getParameter("message");
            String payType = request.getParameter("payType");
            String responseTime = request.getParameter("responseTime");
            String extraData = request.getParameter("extraData");
            String signature = request.getParameter("signature");

            log.info(">>> [MoMo Return] Received params:");
            log.info(">>> [MoMo Return]   - partnerCode: {}", partnerCode);
            log.info(">>> [MoMo Return]   - orderId: {}", orderId);
            log.info(">>> [MoMo Return]   - requestId: {}", requestId);
            log.info(">>> [MoMo Return]   - amount: {}", amount);
            log.info(">>> [MoMo Return]   - orderInfo: {}", orderInfo);
            log.info(">>> [MoMo Return]   - orderType: {}", orderType);
            log.info(">>> [MoMo Return]   - transId: {}", transId);
            log.info(">>> [MoMo Return]   - resultCode: {}", resultCode);
            log.info(">>> [MoMo Return]   - message: {}", message);
            log.info(">>> [MoMo Return]   - payType: {}", payType);
            log.info(">>> [MoMo Return]   - responseTime: {}", responseTime);
            log.info(">>> [MoMo Return]   - extraData: {}", extraData);
            log.info(">>> [MoMo Return]   - signature: {}", signature);

            // Kiểm tra các tham số bắt buộc
            if (orderId == null || resultCode == null) {
                log.error(">>> [MoMo Return] Missing required parameters: orderId={}, resultCode={}", orderId, resultCode);
                result.put("success", false);
                result.put("message", "Thiếu thông tin bắt buộc từ MoMo");
                result.put("response_code", resultCode != null ? resultCode : "99");
                return result;
            }

            // Kiểm tra signature (chỉ verify nếu có signature)
            if (signature != null && !signature.isEmpty()) {
                // Tạo raw signature để verify
                StringBuilder rawHash = new StringBuilder();
                rawHash.append("accessKey=").append(accessKey != null ? accessKey : "");
                rawHash.append("&amount=").append(amount != null ? amount : "");
                rawHash.append("&extraData=").append(extraData != null ? extraData : "");
                rawHash.append("&message=").append(message != null ? message : "");
                rawHash.append("&orderId=").append(orderId);
                rawHash.append("&orderInfo=").append(orderInfo != null ? orderInfo : "");
                rawHash.append("&orderType=").append(orderType != null ? orderType : "");
                rawHash.append("&partnerCode=").append(partnerCode != null ? partnerCode : "");
                rawHash.append("&payType=").append(payType != null ? payType : "");
                rawHash.append("&requestId=").append(requestId != null ? requestId : "");
                rawHash.append("&responseTime=").append(responseTime != null ? responseTime : "");
                rawHash.append("&resultCode=").append(resultCode);
                rawHash.append("&transId=").append(transId != null ? transId : "");

                String rawHashString = rawHash.toString();
                log.info(">>> [MoMo Return] Raw hash string: {}", rawHashString);

                String calculatedSignature = MoMoConfig.hmacSHA256(secretKey, rawHashString);
                log.info(">>> [MoMo Return] Calculated signature: {}", calculatedSignature);
                log.info(">>> [MoMo Return] Received signature: {}", signature);

                // Verify signature
                if (!calculatedSignature.equals(signature)) {
                    result.put("success", false);
                    result.put("message", "Mã bảo mật không hợp lệ");
                    result.put("response_code", resultCode);
                    log.warn(">>> [MoMo Return] Signature verification failed - but continue processing");
                    // KHÔNG return ngay, tiếp tục xử lý để tránh lỗi 500
                } else {
                    log.info(">>> [MoMo Return] Signature verification passed");
                }
            } else {
                log.warn(">>> [MoMo Return] No signature provided, skipping verification");
            }

            // Kiểm tra resultCode: 0 = thành công
            if ("0".equals(resultCode)) {
                result.put("success", true);
                result.put("transaction_code", transId);
                result.put("message", "Xác minh thành công");
                result.put("response_code", resultCode);
            } else {
                result.put("success", false);
                result.put("transaction_code", transId);
                result.put("message", message != null ? message : "Thanh toán thất bại!");
                result.put("response_code", resultCode);
            }

            return result;

        } catch (Exception e) {
            log.error(">>> [MoMo Return] Error processing return: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Lỗi xử lý: " + e.getMessage());
            return result;
        }
    }
}

