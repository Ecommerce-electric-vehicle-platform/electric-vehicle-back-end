package Green_trade.green_trade_platform.mapper;

import Green_trade.green_trade_platform.model.PostProduct;
import Green_trade.green_trade_platform.response.PostProductResponse;
import org.hibernate.query.Page;
import org.springframework.stereotype.Component;

@Component
public class PostProductMapper {
    public PostProductResponse toDto(PostProduct postProduct) {
        return PostProductResponse.builder()
                .postId(postProduct.getId())
                .sellerId(postProduct.getSeller().getSellerId())
                .sellerStoreName(postProduct.getSeller().getStoreName())
                .title(postProduct.getTitle())
                .brand(postProduct.getBrand())
                .model(postProduct.getModel())
                .manufactureYear((postProduct.getManufactureYear()))
                .usedDuration(postProduct.getUsedDuration())
                .rejectedReason(postProduct.getRejectedReason())
                .conditionLevel(postProduct.getConditionLevel())
                .verifiedDecisionStatus(postProduct.getVerifiedDecisionstatus())
                .verified(postProduct.isVerified())
                .active(postProduct.isActive())
                .categoryName(postProduct.getCategory().getName())
                .build();
    }
}
