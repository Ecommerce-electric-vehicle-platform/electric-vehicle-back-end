package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.service.implement.GhnServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/shipping")
@Slf4j
public class ShippingServiceController {

    private final GhnServiceImpl ghnService;
    private final ResponseMapper responseMapper;

    public ShippingServiceController(GhnServiceImpl ghnService, ResponseMapper responseMapper) {
        this.ghnService = ghnService;
        this.responseMapper = responseMapper;
    }

    @GetMapping("/provinces")
    public ResponseEntity<?> getProvinces() throws JsonProcessingException {
        Map<String, String> provincesMap = new HashMap<>();
        String provincesInString = ghnService.getProvinces();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(provincesInString);
        JsonNode data = root.path("data");

        for (JsonNode province : data) {
            int id = province.path("ProvinceID").asInt();
            String name = province.path("ProvinceName").asText();
            provincesMap.put(id + "", name);
        }
        RestResponse response = responseMapper.toDto(
                true,
                "FETCH PROVINCES SUCCESSFULLY",
                provincesMap,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @GetMapping("/provinces")
    public ResponseEntity<?> getProvinces() throws JsonProcessingException {
        Map<String, String> provincesMap = new HashMap<>();
        String provincesInString = ghnService.getProvinces();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(provincesInString);
        JsonNode data = root.path("data");

        for (JsonNode province : data) {
            int id = province.path("ProvinceID").asInt();
            String name = province.path("ProvinceName").asText();
            provincesMap.put(id + "", name);
        }
        RestResponse response = responseMapper.toDto(
                true,
                "FETCH PROVINCES SUCCESSFULLY",
                provincesMap,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }
}
