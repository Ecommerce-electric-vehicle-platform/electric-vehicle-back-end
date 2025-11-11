package Green_trade.green_trade_platform.service;

import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.Order;
import Green_trade.green_trade_platform.model.Wallet;
import Green_trade.green_trade_platform.request.PlaceOrderRequest;
import Green_trade.green_trade_platform.request.ProfileRequest;
import Green_trade.green_trade_platform.request.UpdateBuyerProfileRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Map;

public interface BuyerService {
    Map<String, Object> uploadBuyerProfile(ProfileRequest request, MultipartFile avatarFile) throws Exception;

    Buyer updateProfile(UpdateBuyerProfileRequest request, MultipartFile avatarFile) throws Exception;

    Buyer getCurrentUser();

    Buyer findBuyerById(Long id);

    Buyer getBuyerFromVnPayRequest(String vnpOtherType);

    BigDecimal getWalletBalance();

    boolean isBuyerExisted(Long buyerId);

    boolean isBuyerExisted(String username);

    Order placeOrderCOD(PlaceOrderRequest request) throws Exception;

    Order placeOrder(PlaceOrderRequest request, String shippingFee) throws Exception;

    Order updateOrderCode(Order newOrder, String shippingCode);

    Buyer findBuyerByUsername(String username);

    Buyer findBuyerBySellerId(Long sellerId);

    Wallet getWallet();

    Page<Buyer> getListBuyers(int page, int size);

    void blockAccount(long id, String message, String activity);

    int getTotalBuyers();
}

