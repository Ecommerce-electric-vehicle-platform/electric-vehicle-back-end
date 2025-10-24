package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.mapper.ShippingPartnerMapper;
import Green_trade.green_trade_platform.model.PostProduct;
import Green_trade.green_trade_platform.model.ShippingPartner;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.response.ShippingPartnerResponse;
import Green_trade.green_trade_platform.service.implement.ShippingPartnerServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/shipping-partner")
public class ShippingPartnerController {

    private final ShippingPartnerServiceImpl shippingPartnerService;
    private final ShippingPartnerMapper shippingPartnerMapper;
    private final ResponseMapper responseMapper;

    public ShippingPartnerController(ShippingPartnerServiceImpl shippingPartnerService, ShippingPartnerMapper shippingPartnerMapper, ResponseMapper responseMapper) {
        this.shippingPartnerService = shippingPartnerService;
        this.shippingPartnerMapper = shippingPartnerMapper;
        this.responseMapper = responseMapper;
    }

    @GetMapping("/partners")
    public ResponseEntity<RestResponse<List<ShippingPartnerResponse>, Object>> getShippingPartners()  {
        List<ShippingPartnerResponse> responseData = new ArrayList<>();
        List<ShippingPartner> shippingPartners = shippingPartnerService.getShippingPartners();
        shippingPartners.forEach(
                shippingPartner -> responseData.add(shippingPartnerMapper.toDto(shippingPartner))
        );
        RestResponse<List<ShippingPartnerResponse>, Object> response = responseMapper.toDto(
                true,
                "FETCH SHIPPING PARTNER SUCCESSFULLY",
                responseData,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }
}
