package Green_trade.green_trade_platform.service;

import Green_trade.green_trade_platform.model.Order;
import Green_trade.green_trade_platform.model.Review;
import Green_trade.green_trade_platform.request.ReviewRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReviewService {
    Review createReview(ReviewRequest request, List<MultipartFile> reviewImages);

    Review getReviewsByOrderId(Long orderId);
}

