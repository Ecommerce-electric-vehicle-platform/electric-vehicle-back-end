package Green_trade.green_trade_platform.mapper;

import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.Seller;
import Green_trade.green_trade_platform.request.UpgradeRequest;
import Green_trade.green_trade_platform.response.SellerResponse;
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
                .storePolicyUrl(policy)
                .build();
    }

    public SellerResponse toDto(Seller seller) {
        if (seller == null) return null;

        Buyer buyer = seller.getBuyer();

        return SellerResponse.builder()
                .sellerId(seller.getSellerId())
                .storeName(seller.getStoreName())
                .status(seller.getStatus())
                .storePolicyUrl(seller.getStorePolicyUrl())
                .taxNumber(seller.getTaxNumber())
                .createAt(seller.getCreateAt())
                .updateAt(seller.getUpdateAt())
                .identityFrontImageUrl(seller.getIdentityFrontImageUrl())
                .identityBackImageUrl(seller.getIdentityBackImageUrl())
                .businessLicenseUrl(seller.getBusinessLicenseUrl())
                .selfieUrl(seller.getSelfieUrl())
                .build();
    }
}
