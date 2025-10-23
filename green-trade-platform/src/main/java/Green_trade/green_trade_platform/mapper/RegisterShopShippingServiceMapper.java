package Green_trade.green_trade_platform.mapper;

import Green_trade.green_trade_platform.model.Seller;
import Green_trade.green_trade_platform.service.implement.GhnServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RegisterShopShippingServiceMapper {
    private final GhnServiceImpl ghnService;

    public RegisterShopShippingServiceMapper(GhnServiceImpl ghnService) {
        this.ghnService = ghnService;
    }

    public Map<String, Object> toDto(Seller seller) throws JsonProcessingException {
        String provinceId = ghnService.findProvinceCodeByProvinceName(seller.getBuyer().getProvinceName());
        String districtId = ghnService.findDistrictCodeByDistrictName(Integer.parseInt(provinceId), seller.getBuyer().getDistrictName());
        Map<String, Object> result = Map.of(
                "district_id", Integer.parseInt(districtId),
                "ward_code", ghnService.findWardCodeByWardName(Integer.parseInt(districtId), seller.getBuyer().getWardName()),
                "name", seller.getStoreName(),
                "phone", seller.getBuyer().getPhoneNumber(),
                "address", seller.getBuyer().getDefaultShippingAddress()
        );
        return result;
    }
}
