package Green_trade.green_trade_platform.mapper;

import Green_trade.green_trade_platform.model.Order;
import Green_trade.green_trade_platform.model.PostProduct;
import Green_trade.green_trade_platform.service.implement.GhnServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GetShippingFeeServiceMapper {
    private final GhnServiceImpl ghnService;

    public GetShippingFeeServiceMapper(GhnServiceImpl ghnService) {
        this.ghnService = ghnService;
    }

    public Map<String, Object> toDto(Order order) throws JsonProcessingException {
        String sellerProvinceId = ghnService.findProvinceCodeByProvinceName(order.getPostProduct().getSeller().getBuyer().getProvinceName());
        String sellerDistrictId = ghnService.findDistrictCodeByDistrictName(Integer.parseInt(sellerProvinceId), order.getPostProduct().getSeller().getBuyer().getDistrictName());
        String sellerWardId = ghnService.findWardCodeByWardName(Integer.parseInt(sellerDistrictId), order.getPostProduct().getSeller().getBuyer().getWardName());

        String buyerProvinceId = ghnService.findProvinceCodeByProvinceName(order.getBuyer().getProvinceName());
        String buyerDistrictId = ghnService.findDistrictCodeByDistrictName(Integer.parseInt(buyerProvinceId), order.getBuyer().getDistrictName());
        String buyerWardId = ghnService.findWardCodeByWardName(Integer.parseInt(buyerDistrictId), order.getBuyer().getWardName());

        Map<String, Object> result = new HashMap<>();
        result.put("service_type_id", 5);
        result.put("from_district_id", Integer.parseInt(sellerDistrictId));
        result.put("from_ward_code", sellerWardId);
        result.put("to_district_id", Integer.parseInt(buyerDistrictId));
        result.put("to_ward_code", buyerWardId);
        result.put("length", 30);
        result.put("width", 40);
        result.put("height", 20);
        result.put("weight", 3000);
        result.put("insurance_value", 0);
        result.put("coupon", null);
        Map<String, Object> item = Map.of(
                "name", order.getPostProduct().getTitle(),
                "quantity", 1,
                "length", 200,
                "width", 200,
                "height", 200,
                "weight", 1000
        );
        result.put("items", List.of(item));
        return result;
    }
}
