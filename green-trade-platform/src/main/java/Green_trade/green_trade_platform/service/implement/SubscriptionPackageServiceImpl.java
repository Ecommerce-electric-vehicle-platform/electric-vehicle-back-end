package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.mapper.SubscriptionPackageMapper;
import Green_trade.green_trade_platform.model.SubscriptionPackages;
import Green_trade.green_trade_platform.repository.SubscriptionPackagesRepository;
import Green_trade.green_trade_platform.response.SubscriptionPackageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionPackageServiceImpl {

    private final SubscriptionPackagesRepository subscriptionPackageRepository;
    private final SubscriptionPackageMapper subscriptionPackageMapper;

    public Page<SubscriptionPackages> getActivePackages(Pageable pageable) {
        return subscriptionPackageRepository.findByIsActiveTrue(pageable);
    }

    public Page<SubscriptionPackageResponse> getActivePackageResponses(Pageable pageable) {
        return getActivePackages(pageable)
                .map(subscriptionPackageMapper::toResponse);
    }
}