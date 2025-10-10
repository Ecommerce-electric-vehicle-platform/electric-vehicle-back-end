package Green_trade.green_trade_platform.util;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class VnPayUtils {
    public static String hmacSHA512(String key, String data) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmac.init(secretKey);
        byte[] hashBytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(2 * hashBytes.length);
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    /**
     * Build query string (encoded) và hashData (raw, không encode) theo thứ tự alphabet (TreeMap).
     * Trả về map chứa: "hashData" và "query".
     */
    public Map<String, String> buildQueryAndHashData(Map<String, String> params) {
        // sort keys
        SortedMap<String, String> sorted = new TreeMap<>(params);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> e : sorted.entrySet()) {
            String key = e.getKey();
            String value = e.getValue();
            if (value == null || value.length() == 0) continue;

            if (!first) {
                hashData.append('&');
                query.append('&');
            } else {
                first = false;
            }

            // raw for hashData
            hashData.append(key).append('=').append(value);

            // encoded for query string
            query.append(URLEncoder.encode(key, StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        }

        Map<String, String> result = new HashMap<>();
        result.put("hashData", hashData.toString());
        result.put("query", query.toString());
        return result;
    }

    public String hashAllFields(Map<String, String> fields, String secretKey) throws Exception {
        // Bỏ trường chữ ký ra khỏi dữ liệu gốc
        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                if (hashData.length() > 0) {
                    hashData.append("&");
                }
                hashData.append(fieldName).append("=").append(fieldValue);
            }
        }

        return hmacSHA512(secretKey, hashData.toString());
    }
}
