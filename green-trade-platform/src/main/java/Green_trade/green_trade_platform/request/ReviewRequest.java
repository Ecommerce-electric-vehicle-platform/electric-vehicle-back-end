package Green_trade.green_trade_platform.request;

import Green_trade.green_trade_platform.model.ReviewImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequest {
    private Long orderId;
    private double rating;
    private String feedback;
    List<ReviewImage> reviewImages;
}
