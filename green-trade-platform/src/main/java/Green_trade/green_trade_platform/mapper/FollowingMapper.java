package Green_trade.green_trade_platform.mapper;

import Green_trade.green_trade_platform.model.Following;
import Green_trade.green_trade_platform.response.FollowingResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class FollowingMapper {
    private final SellerMapper sellerMapper;

    public FollowingResponse toDto(Following following) {
        if (following == null) return null;

        return FollowingResponse.builder()
                .sellerId(following.getSeller().getSellerId())
                .seller(sellerMapper.toDto(following.getSeller()))
                .followedAt(following.getFollowedAt())
                .isFollowing(following.getUnfollowedAt() == null) // true nếu đang follow
                .build();
    }
}

