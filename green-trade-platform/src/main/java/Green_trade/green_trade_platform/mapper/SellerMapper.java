package Green_trade.green_trade_platform.mapper;

import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.Seller;
import Green_trade.green_trade_platform.request.UpgradeRequest;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
public class SellerMapper {
    public Seller toEntity(UpgradeRequest request, Buyer buyer, String frontIdentity,
                           String license, String backIdentity, String selfie, String policy) {
        return Seller.builder().buyer(buyer)
                .identityFrontImageUrl(frontIdentity)
                .businessLicenseUrl(license)
                .identityBackImageUrl(backIdentity)
                .selfieUrl(selfie)
                .storeName(request.getStoreName())
                .taxNumber(request.getTaxNumber())
                .identityNumber(request.getIdentityNumber())
                .build();
    }
}
