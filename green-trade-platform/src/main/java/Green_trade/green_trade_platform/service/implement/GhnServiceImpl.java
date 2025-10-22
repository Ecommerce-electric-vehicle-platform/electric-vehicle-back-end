package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.model.Order;
import Green_trade.green_trade_platform.request.CancelOrderRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
@Slf4j
public class GhnServiceImpl {

    private static final String TOKEN = "4433d6f4-ae5f-11f0-b040-4e257d8388b4";
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
        headers.set("Token", "4433d6f4-ae5f-11f0-b040-4e257d8388b4");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = "https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/ward?district_id=" + districtId;

        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,   // GET là chuẩn nhất ở đây
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

    public Map<String, String> getProvinceList() throws JsonProcessingException {
        Map<String, String> result = new HashMap<>();
        String provincesInString = getProvinces();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(provincesInString);
        JsonNode data = root.path("data");

        for (JsonNode province : data) {
            int id = province.path("ProvinceID").asInt();
            String name = province.path("ProvinceName").asText();
            result.put(id + "", name);
        }
        return result;
    }

    public Map<String, String> getDistrictListByProvinceId(int provinceId) throws JsonProcessingException {
        Map<String, String> result = new HashMap<>();
        String districtsInString = getDistricts(provinceId);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(districtsInString);
        JsonNode data = root.path("data");

        for (JsonNode province : data) {
            int id = province.path("DistrictID").asInt();
            String name = province.path("DistrictName").asText();
            result.put(id + "", name);
        }
        return result;
    }

    public Map<String, String> getWardListByDistrictId(int districtId) throws JsonProcessingException {
        Map<String, String> result = new HashMap<>();
        String districtsInString = getWards(districtId);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(districtsInString);
        JsonNode data = root.path("data");

        for (JsonNode province : data) {
            int id = province.path("WardCode").asInt();
            String name = province.path("WardName").asText();
            result.put(id + "", name);
        }
        return result;
    }

    public String findProvinceCodeByProvinceName(String provinceName) throws JsonProcessingException {
        Map<String, String> provinceList = getProvinceList();

        // Duyệt qua danh sách để tìm tỉnh có tên khớp
        for (Map.Entry<String, String> entry : provinceList.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(provinceName.trim())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public String findDistrictCodeByDistrictName(int provinceId, String districtName) throws JsonProcessingException {
        Map<String, String> districtList = getDistrictListByProvinceId(provinceId);

        for (Map.Entry<String, String> entry : districtList.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(districtName.trim())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public String findWardCodeByWardName(int districtId, String wardName) throws JsonProcessingException {
        Map<String, String> wardList = getWardListByDistrictId(districtId);

        for (Map.Entry<String, String> entry : wardList.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(wardName.trim())) {
                return entry.getKey(); // WardCode là String, không cần parse sang Long
            }
        }
        return null;
    }

    public Map<String, String> getShippingFeeDto(Order order) throws JsonProcessingException {
        Map<String, Object> bodyData = getShippingFeeServiceBodyRequest(order);

        String resultString = getShippingFee(bodyData, order.getPostProduct().getSeller().getGhnShopId());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(resultString);

        if (root == null || !root.has("code")) {
            throw new RuntimeException("Phản hồi không hợp lệ từ GHN: " + resultString);
        }

        int code = root.path("code").asInt();
        if (code != 200) {
            String message = root.path("message").asText("Unknown error");
            throw new RuntimeException("GHN API trả về lỗi: " + message);
        }

        JsonNode data = root.path("data");

        Map<String, String> result = new LinkedHashMap<>();
        result.put("message", root.path("message").asText());
        result.put("total", data.path("total").asText());
        result.put("service_fee", data.path("service_fee").asText());
        result.put("insurance_fee", data.path("insurance_fee").asText());
        result.put("pick_station_fee", data.path("pick_station_fee").asText());
        result.put("coupon_value", data.path("coupon_value").asText());
        result.put("r2s_fee", data.path("r2s_fee").asText());
        result.put("cod_fee", data.path("cod_fee").asText());
        result.put("pick_remote_areas_fee", data.path("pick_remote_areas_fee").asText());
        result.put("deliver_remote_areas_fee", data.path("deliver_remote_areas_fee").asText());
        result.put("cod_failed_fee", data.path("cod_failed_fee").asText());

        log.info("GHN shipping fee response mapped: {}", result);

        return result;
    }

    public Map<String, Object> getShippingFeeServiceBodyRequest(Order order) throws JsonProcessingException {
        String sellerProvinceId = findProvinceCodeByProvinceName(order.getPostProduct().getSeller().getBuyer().getProvinceName());
        String sellerDistrictId = findDistrictCodeByDistrictName(Integer.parseInt(sellerProvinceId), order.getPostProduct().getSeller().getBuyer().getDistrictName());
        String sellerWardId = findWardCodeByWardName(Integer.parseInt(sellerDistrictId), order.getPostProduct().getSeller().getBuyer().getWardName());

        String buyerProvinceId = findProvinceCodeByProvinceName(order.getBuyer().getProvinceName());
        String buyerDistrictId = findDistrictCodeByDistrictName(Integer.parseInt(buyerProvinceId), order.getBuyer().getDistrictName());
        String buyerWardId = findWardCodeByWardName(Integer.parseInt(buyerDistrictId), order.getBuyer().getWardName());

        Map<String, Object> result = new HashMap<>();
        result.put("service_type_id", 5);
        result.put("from_district_id", Integer.parseInt(sellerDistrictId));
        result.put("from_ward_code", sellerWardId);
        result.put("to_district_id", Integer.parseInt(buyerDistrictId));
        result.put("to_ward_code", buyerWardId);
        result.put("length", order.getPostProduct().getLength());
        result.put("width", order.getPostProduct().getWidth());
        result.put("height", order.getPostProduct().getHeight());
        result.put("weight", order.getPostProduct().getWeight());
        result.put("insurance_value", 0);
        result.put("coupon", null);
        Map<String, Object> item = Map.of(
                "name", order.getPostProduct().getTitle(),
                "quantity", 1,
                "length", order.getPostProduct().getLength(),
                "width", order.getPostProduct().getWidth(),
                "height", order.getPostProduct().getHeight(),
                "weight", order.getPostProduct().getWeight()
        );
        result.put("items", List.of(item));
        return result;
    }



}

