package Green_trade.green_trade_platform.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowingResponse {
    private Long sellerId;
    private SellerResponse seller;
    private LocalDateTime followedAt;
    private boolean isFollowing; // Always true for active followings
}

