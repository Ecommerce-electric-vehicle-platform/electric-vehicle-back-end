package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.mapper.SubscriptionMapper;
import Green_trade.green_trade_platform.model.Subscription;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.response.SubscriptionResponse;
import Green_trade.green_trade_platform.service.implement.SellerServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SellerController {

    private final ResponseMapper responseMapper;
    private SellerServiceImpl sellerService;

    public SellerController(SellerServiceImpl sellerService, ResponseMapper responseMapper, SubscriptionMapper subscriptionMapper) {
        this.sellerService = sellerService;
        this.responseMapper = responseMapper;
    }

    @PostMapping("/{username}/check-service-package-validity")
    public ResponseEntity<RestResponse<?, ?>> checkServicePackageValidity(@RequestBody Long id) throws Exception {
        SubscriptionResponse result = sellerService.checkServicePackageValidity(id);
//        RestResponse response = responseMapper.toDto(true, "Service Package is valid", subscriptionMapper.toDto())
        return ResponseEntity.status(HttpStatus.OK.value()).body(null);
    }
}
