package Green_trade.green_trade_platform.service;

import Green_trade.green_trade_platform.model.Order;
import Green_trade.green_trade_platform.model.Review;
import Green_trade.green_trade_platform.model.Seller;
import Green_trade.green_trade_platform.request.ReviewRequest;
import Green_trade.green_trade_platform.request.UpdateReviewRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReviewService {
    Review createReview(ReviewRequest request, List<MultipartFile> reviewImages);

    Review getReviewsByOrderId(Long orderId);

    Review getReviewById(Long reviewId);

    Page<Review> getReviewsBySeller(Seller seller, int page, int size);

    Review updateReview(Long reviewId, UpdateReviewRequest request, List<MultipartFile> newImages);
}

