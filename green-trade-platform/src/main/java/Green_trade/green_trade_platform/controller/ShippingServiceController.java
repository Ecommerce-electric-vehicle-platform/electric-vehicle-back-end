package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.model.Order;
import Green_trade.green_trade_platform.repository.OrderRepository;
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
    private final OrderRepository orderRepository;

    public ShippingServiceController(GhnServiceImpl ghnService, ResponseMapper responseMapper, OrderRepository orderRepository) {
        this.ghnService = ghnService;
        this.responseMapper = responseMapper;
        this.orderRepository = orderRepository;
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

    @GetMapping("/shipping-fee/{orderId}")
    public ResponseEntity<?> getShippingFee(
            @PathVariable Long orderId
    ) throws Exception {
        int codValue = 0;
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new Exception("Order is not Existed"));
        Map<String, String> shippingFeeData = ghnService.getShippingFeeDto(order, codValue);
        RestResponse response = responseMapper.toDto(
                true,
                "FETCH SHIPPING FEE SUCCESSFULLY",
                shippingFeeData,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }
}
