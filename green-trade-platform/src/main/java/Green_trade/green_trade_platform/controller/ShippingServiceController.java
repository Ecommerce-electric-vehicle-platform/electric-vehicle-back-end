package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.exception.OrderNotFound;
import Green_trade.green_trade_platform.exception.PaymentMethodNotSupportedException;
import Green_trade.green_trade_platform.exception.PostProductNotFound;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.model.*;
import Green_trade.green_trade_platform.repository.OrderRepository;
import Green_trade.green_trade_platform.repository.PostProductRepository;
import Green_trade.green_trade_platform.request.ShippingFeeRequest;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.service.PostProductService;
import Green_trade.green_trade_platform.service.implement.BuyerServiceImpl;
import Green_trade.green_trade_platform.service.implement.GhnServiceImpl;
import Green_trade.green_trade_platform.service.implement.PaymentServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
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
    private final PostProductService postProductService;
    private final PostProductRepository postProductRepository;
    private final BuyerServiceImpl buyerService;
    private final PaymentServiceImpl paymentService;

    public ShippingServiceController(GhnServiceImpl ghnService, ResponseMapper responseMapper, OrderRepository orderRepository, PostProductService postProductService, PostProductRepository postProductRepository, BuyerServiceImpl buyerService, PaymentServiceImpl paymentService) {
        this.ghnService = ghnService;
        this.responseMapper = responseMapper;
        this.orderRepository = orderRepository;
        this.postProductService = postProductService;
        this.postProductRepository = postProductRepository;
        this.buyerService = buyerService;
        this.paymentService = paymentService;
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
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFound());
        Map<String, String> shippingFeeData = ghnService.getShippingFeeDto(order, codValue);
        RestResponse response = responseMapper.toDto(
                true,
                "FETCH SHIPPING FEE SUCCESSFULLY",
                shippingFeeData,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @GetMapping("/shipping-fee")
    public ResponseEntity<RestResponse<Map<String, String>, Object>> getShippingFee(
            @Valid @RequestBody ShippingFeeRequest request
    ) throws Exception {
        int codValue = 0;
        log.info(">>> [ShippingServiceController] in getShippingFee: codValue = {}", codValue);
        PostProduct postProduct = postProductRepository.findById(request.getPostId()).orElseThrow(() -> new PostProductNotFound());

        Buyer currentBuyer = buyerService.getCurrentUser();
        currentBuyer.setWardName(request.getWardName());
        currentBuyer.setDistrictName(request.getDistrictName());
        currentBuyer.setDistrictName(request.getDistrictName());

        Seller seller = postProduct.getSeller();

        Payment payment = paymentService.findPaymentMethodById(request.getPaymentId());

        if(payment == null) {
            throw new PaymentMethodNotSupportedException();
        }
        log.info(">>> [ShippingServiceController] in getShippingFee: Payment is supported");


        if(payment.getGatewayName().equalsIgnoreCase("COD")) {
            log.info(">>> [ShippingServiceController] in getShippingFee: COD payment");
            codValue = postProduct.getPrice().intValue();
        }
        log.info(">>> [ShippingServiceController] in getShippingFee: codValue = {}", codValue);

        Map<String, String> shippingFeeData = ghnService.getShippingFeeDto(currentBuyer, seller, postProduct, codValue);

        RestResponse<Map<String, String>, Object> response = responseMapper.toDto(
                true,
                "FETCH SHIPPING FEE SUCCESSFULLY",
                shippingFeeData,
                null
        );

        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }
}
