package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.mapper.SubscriptionPackageMapper;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.SubscriptionPackages;
import Green_trade.green_trade_platform.repository.SubscriptionPackagesRepository;
import Green_trade.green_trade_platform.request.SignPackageRequest;
import Green_trade.green_trade_platform.response.SubscriptionPackageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SubscriptionPackageServiceImpl {

    private final SubscriptionPackagesRepository subscriptionPackageRepository;
    private final SubscriptionPackageMapper subscriptionPackageMapper;
    private final BuyerServiceImpl buyerService;
    private final WalletServiceImpl walletService;

    public Page<SubscriptionPackages> getActivePackages(Pageable pageable) {
        return subscriptionPackageRepository.findByIsActiveTrue(pageable);
    }

    public Page<SubscriptionPackageResponse> getActivePackageResponses(Pageable pageable) {
        return getActivePackages(pageable)
                .map(subscriptionPackageMapper::toResponse);
    }

    public Map<String, Object> signPackage(SignPackageRequest request) {
        Map<String, Object> result = new HashMap<>();
        boolean isValidBalance = isValidWalletBalance(request);
        if(!isValidBalance) {
            result.put("success", false);
            result.put("message", "Số dư ví không đủ, vui lòng nạp thêm.");
            return result;
        }

        Buyer buyer = buyerService.getCurrentUser();
        Map<String, Object> result = walletService.handleSignPackageForSeller(buyer, request.getPrice());
    }

    public boolean isValidWalletBalance(SignPackageRequest request) {
        BigDecimal walletBalance = buyerService.getWalletBalance();
        return (walletBalance.doubleValue() >= request.getPrice());
    }
}