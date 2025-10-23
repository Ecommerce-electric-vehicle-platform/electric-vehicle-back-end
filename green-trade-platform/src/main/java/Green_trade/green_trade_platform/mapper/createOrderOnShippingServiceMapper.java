package Green_trade.green_trade_platform.mapper;

import Green_trade.green_trade_platform.model.*;
import Green_trade.green_trade_platform.service.implement.GhnServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class createOrderOnShippingServiceMapper {
    private final GhnServiceImpl ghnService;

    public createOrderOnShippingServiceMapper(GhnServiceImpl ghnService) {
        this.ghnService = ghnService;
    }

    public Map<String, Object> toDto(Order order, Payment paymentMethod) throws JsonProcessingException {
        Map<String, Object> data = new HashMap<>();
        Seller seller = order.getPostProduct().getSeller();
        Buyer buyer = order.getBuyer();
        PostProduct postProduct = order.getPostProduct();
        String sellerProvinceId = ghnService.findProvinceCodeByProvinceName(seller.getBuyer().getProvinceName());
        String sellerDistrictId = ghnService.findDistrictCodeByDistrictName(Integer.parseInt(sellerProvinceId), seller.getBuyer().getDistrictName());
        String sellerWardId = ghnService.findWardCodeByWardName(Integer.parseInt(sellerDistrictId), seller.getBuyer().getWardName());
        String buyerProvinceId = ghnService.findProvinceCodeByProvinceName(buyer.getProvinceName());
        String buyerDistrictId = ghnService.findDistrictCodeByDistrictName(Integer.parseInt(buyerProvinceId), buyer.getDistrictName());
        String buyerWardId = ghnService.findWardCodeByWardName(Integer.parseInt(buyerDistrictId), buyer.getWardName());
        int codValue = 0;
        if("COD".equalsIgnoreCase(paymentMethod.getGatewayName())) {
            codValue = order.getPrice().intValue();
        }


        data.put("payment_type_id", 2);
        data.put("note", "Not have"); //lưu ý vì chưa có tham số truyền vào
        data.put("required_note", "KHONGCHOXEMHANG");
        data.put("return_phone", seller.getBuyer().getPhoneNumber());
        data.put("return_address", seller.getBuyer().getDefaultShippingAddress());
        data.put("return_district_id", "");
        data.put("return_ward_code", null);
        data.put("client_order_code", "");
        data.put("from_name", seller.getBuyer().getFullName());
        data.put("from_phone", seller.getBuyer().getPhoneNumber());
        data.put("from_address", seller.getBuyer().getDefaultShippingAddress());
        data.put("from_ward_name", seller.getBuyer().getWardName());
        data.put("from_district_name", seller.getBuyer().getDistrictName());
        data.put("from_province_name", seller.getBuyer().getProvinceName());
        data.put("to_name", buyer.getFullName());
        data.put("to_phone", buyer.getPhoneNumber());
        data.put("to_address", buyer.getDefaultShippingAddress());
        data.put("to_ward_name", buyer.getWardName());
        data.put("to_district_name", buyer.getDistrictName());
        data.put("to_province_name", buyer.getProvinceName());
        data.put("cod_amount", codValue);
        data.put("content", order.getPostProduct().getTitle());
        data.put("length", postProduct.getLength());
        data.put("width", postProduct.getWidth());
        data.put("height", postProduct.getHeight());
        data.put("weight", postProduct.getWeight());
        data.put("cod_failed_amount", 2000);
        data.put("pick_station_id", 1444);
        data.put("deliver_station_id", null);
        data.put("insurance_value", 0);
        data.put("service_type_id", 5);
        data.put("coupon", null);
        data.put("pickup_time", 1692840132);
        data.put("pick_shift", List.of(2));

        // Items
        Map<String, Object> item = new HashMap<>();
        item.put("name", order.getPostProduct().getTitle());
        item.put("code", Integer.parseInt(order.getOrderCode()));
        item.put("quantity", 1);
        item.put("price", postProduct.getPrice());
        item.put("length", postProduct.getLength());
        item.put("width", postProduct.getWidth());
        item.put("height", postProduct.getHeight());
        item.put("weight", postProduct.getWeight());

        Map<String, Object> category = new HashMap<>();
        category.put("level1", order.getPostProduct().getTitle());
        item.put("category", category);

        data.put("items", List.of(item));

        return data;
    }

}
