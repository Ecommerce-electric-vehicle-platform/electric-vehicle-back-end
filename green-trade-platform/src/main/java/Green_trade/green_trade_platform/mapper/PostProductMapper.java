package Green_trade.green_trade_platform.mapper;

import Green_trade.green_trade_platform.model.PostProduct;
import Green_trade.green_trade_platform.response.PostProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

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
                .price(postProduct.getPrice())
                .locationTrading(postProduct.getLocationTrading())
                .categoryName(postProduct.getCategory().getName())
                .build();
    }

    public Page<PostProductResponse> toDtoPage(Page<PostProduct> postProductPage) {
        List<PostProductResponse> responses = postProductPage.getContent()
                .stream()
                .map(this::toDto)
                .toList();

        Pageable pageable = postProductPage.getPageable();

        return new PageImpl<>(responses, pageable, postProductPage.getTotalElements());
    }
}
