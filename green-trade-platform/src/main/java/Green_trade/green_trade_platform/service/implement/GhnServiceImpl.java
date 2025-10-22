package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.request.CancelOrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
@Slf4j
public class GhnServiceImpl {

    private static final String TOKEN = "285518-c4bb-11ea-be3a-f636b1deefb9";
    private static final String SHOP_ID = "885";

    public String createOrder(Map<String, Object> requestBody, String shopId) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", TOKEN);
        headers.set("ShopId", shopId);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/create", entity, String.class);

        return response.getBody();
    }

    public String cancelOrder(CancelOrderRequest request, String shopId) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", TOKEN);
        headers.set("ShopId", shopId);

        HttpEntity<CancelOrderRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity("https://dev-online-gateway.ghn.vn/shiip/public-api/v2/switch-status/cancel", entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    public String getShippingFee(Map<String, Object> requestBody, String shopId) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", TOKEN);
        headers.set("ShopId", shopId);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity("https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee", entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    public String registerShop(Map<String, Object> requestBody) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", TOKEN);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity("https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shop/register", entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    public String getProvinces() {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", "4433d6f4-ae5f-11f0-b040-4e257d8388b4");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(
                            "https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/province",
                            HttpMethod.GET,
                            entity,
                            String.class
                    );

            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    public String getWards(int districtId) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", "4433d6f4-ae5f-11f0-b040-4e257d8388b4");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("district_id", districtId);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            "https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/ward",
                            entity,
                            String.class
                    );

            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    public String getDistricts(int provinceId) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", "4433d6f4-ae5f-11f0-b040-4e257d8388b4");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("province_id", provinceId);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/district",
                    HttpMethod.POST,  // Dù curl ghi GET, GHN yêu cầu POST khi có body
                    entity,
                    String.class
            );

            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

}

