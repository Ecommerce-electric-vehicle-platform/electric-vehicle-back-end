package Green_trade.green_trade_platform.service;

import Green_trade.green_trade_platform.model.Seller;
import Green_trade.green_trade_platform.model.Subscription;
import Green_trade.green_trade_platform.model.SubscriptionPackages;
import Green_trade.green_trade_platform.request.SignPackageRequest;
import Green_trade.green_trade_platform.response.SubscriptionPackageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface SubscriptionPackageService {
    Page<SubscriptionPackages> getActivePackages(Pageable pageable);

    Page<SubscriptionPackageResponse> getActivePackageResponses(Pageable pageable);

    Map<String, Object> handlesignPackage(SignPackageRequest request);

    boolean isValidWalletBalance(SignPackageRequest request);

    void cancelSubscription(Seller seller);

    Subscription getCurrentSubscription(Seller seller);

    Subscription updateRemainPost(Seller seller);

    double getTotalRevenue();
}

