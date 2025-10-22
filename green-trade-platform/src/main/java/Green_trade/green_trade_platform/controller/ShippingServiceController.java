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
import org.springframework.web.bind.annotation.*;

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
        provincesMap = ghnService.getProvinceList();
        RestResponse response = responseMapper.toDto(
                true,
                "FETCH PROVINCES SUCCESSFULLY",
                provincesMap,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @GetMapping("/districts")
    public ResponseEntity<?> getDistricts(
            @RequestParam int provinceId
    ) throws JsonProcessingException {
        Map<String, String> districtsMap = new HashMap<>();
        districtsMap = ghnService.getDistrictListByProvinceId(provinceId);
        RestResponse response = responseMapper.toDto(
                true,
                "FETCH DISTRICTS SUCCESSFULLY",
                districtsMap,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @GetMapping("/wards")
    public ResponseEntity<?> getWards(
            @RequestParam int districtId
    ) throws JsonProcessingException {
        Map<String, String> wardsMap = new HashMap<>();
        wardsMap = ghnService.getWardListByDistrictId(districtId);
        RestResponse response = responseMapper.toDto(
                true,
                "FETCH WARDS SUCCESSFULLY",
                wardsMap,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }
}
