package Green_trade.green_trade_platform.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Random;

@Component
public class MoMoConfig {
    @Value("${momo.partner-code:MOMO}")
    private String partnerCodeValue;

    @Value("${momo.access-key:F8BBA842ECF85}")
    private String accessKeyValue;

    @Value("${momo.secret-key:K951B6PE1waDMi640xX08PD3vg6EkVlz}")
    private String secretKeyValue;

    @Value("${momo.endpoint:https://test-payment.momo.vn/v2/gateway/api/create}")
    private String endpointValue;

    @Value("${momo.return-url:http://localhost:8080/api/v1/momo/return}")
    private String returnUrlValue;

    @Value("${momo.ipn-url:http://localhost:8080/api/v1/momo/ipn}")
    private String ipnUrlValue;

    // Static fields for backward compatibility
    public static String partnerCode = "MOMO";
    public static String accessKey = "F8BBA842ECF85";
    public static String secretKey = "K951B6PE1waDMi640xX08PD3vg6EkVlz";
    public static String endpoint = "https://test-payment.momo.vn/v2/gateway/api/create";
    public static String returnUrl = "http://localhost:8080/api/v1/momo/return";
    public static String ipnUrl = "http://localhost:8080/api/v1/momo/ipn";

    @PostConstruct
    public void init() {
        // Initialize static values from @Value after dependency injection
        partnerCode = partnerCodeValue;
        accessKey = accessKeyValue;
        secretKey = secretKeyValue;
        endpoint = endpointValue;
        returnUrl = returnUrlValue;
        ipnUrl = ipnUrlValue;
    }

    /**
     * Tạo random string cho orderId
     */
    public static String getRandomNumber(int len) {
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        Random rnd = new Random();
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Tạo HMAC SHA256 cho MoMo
     */
    public static String hmacSHA256(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hashBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hash.append('0');
                hash.append(hex);
            }
            return hash.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error while generating HMAC SHA256", e);
        }
    }

    /**
     * Tạo SHA256 hash
     */
    public static String sha256(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(message.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error while generating SHA256", e);
        }
    }
}

