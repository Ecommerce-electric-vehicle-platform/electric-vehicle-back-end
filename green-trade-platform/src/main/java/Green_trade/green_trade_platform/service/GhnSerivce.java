package Green_trade.green_trade_platform.service;

import Green_trade.green_trade_platform.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;

public interface GhnSerivce {
    String createOrder(Map<String, Object> requestBody, String shopId);

    String cancelOrder(Map<String, Object> request, String shopId);

    String getOrderDetail(String orderCode);

    Map<String, Object> extractLatestOrderStatus(String ghnResponse);

    Map<String, Object> getLastestOrderStatus(String orderCode);

    Map<String, Object> createCancelOrderShippingServiceBodyRequest(String orderCode);

    Map<String, Object> createCancelOrderShippingServiceResponseToDto(String orderCode, String shopId) throws JsonProcessingException;

    String getShippingFee(Map<String, Object> requestBody, String shopId);

    String registerShop(Map<String, Object> requestBody);

    String getProvinces();

    String getWards(int districtId);

    String getDistricts(int provinceId);

    Map<String, String> getProvinceList() throws JsonProcessingException;

    Map<String, String> getDistrictListByProvinceId(int provinceId) throws JsonProcessingException;

    Map<String, String> getWardListByDistrictId(int districtId) throws JsonProcessingException;

    String findProvinceCodeByProvinceName(String provinceName) throws JsonProcessingException;

    String findDistrictCodeByDistrictName(int provinceId, String districtName) throws JsonProcessingException;

    String findWardCodeByWardName(int districtId, String wardName) throws JsonProcessingException;

    Map<String, String> getShippingFeeDto(Order order, int codValue) throws JsonProcessingException;

    Map<String, String> getShippingFeeDto(Buyer buyer, Seller seller, PostProduct postProduct, int codValue)
            throws JsonProcessingException;

    Map<String, Object> getShippingFeeServiceBodyRequest(Order order, int codValue)
            throws JsonProcessingException;

    Map<String, Object> getShippingFeeServiceBodyRequest(Buyer buyer, Seller seller, PostProduct postProduct,
                                                         int codValue) throws JsonProcessingException;

    Map<String, Object> createOrderShippingServiceBodyRequest(Order order, Payment paymentMethod)
            throws JsonProcessingException;

    Map<String, String> createOrderShippingResponseToDto(Order order, Payment paymentMethod)
            throws JsonProcessingException;

    String getLastestOrderStatusOnlyStatus(String orderCode);
}
