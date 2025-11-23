package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.controller.NotificationSocketController;
import Green_trade.green_trade_platform.enumerate.AccountType;
import Green_trade.green_trade_platform.filter.BadWordFilter;
import Green_trade.green_trade_platform.model.Notification;
import Green_trade.green_trade_platform.model.Order;
import Green_trade.green_trade_platform.model.Review;
import Green_trade.green_trade_platform.model.ReviewImage;
import Green_trade.green_trade_platform.model.Seller;
import Green_trade.green_trade_platform.repository.OrderRepository;
import Green_trade.green_trade_platform.repository.ReviewImagesRepository;
import Green_trade.green_trade_platform.repository.ReviewRepository;
import Green_trade.green_trade_platform.request.MailRequest;
import Green_trade.green_trade_platform.request.ReviewRequest;
import Green_trade.green_trade_platform.request.UpdateReviewRequest;
import Green_trade.green_trade_platform.service.MailService;
import Green_trade.green_trade_platform.service.NotificationService;
import Green_trade.green_trade_platform.service.ReviewService;
import Green_trade.green_trade_platform.mapper.ReviewMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
@AllArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final BadWordFilter badWordFilter;
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ReviewMapper reviewMapper;
    private final CloudinaryService cloudinaryService;
    private final ReviewImagesRepository reviewImagesRepository;
    private final NotificationService notificationService;
    private final MailService mailService;
    private final NotificationSocketController notificationSocketController;

    public Review createReview(ReviewRequest request, List<MultipartFile> reviewImages) {
        log.info(">>> [Review Service] Create Review: Started.");
        Order order = validateReviewRequest(request);

        if (badWordFilter.containsBadWord(request.getFeedback())) {
            throw new IllegalArgumentException("This feedback contains bad words. Please write again.");
        }

        log.info(">>> [Review Service] Upload images into Cloudinary: Started.");
        Map<String, Object> uploadResult = uploadReviewImages(reviewImages);

        // ✅ Extract uploaded images list
        List<Map<String, String>> uploadedFiles = (List<Map<String, String>>) uploadResult.get("uploaded");
        List<ReviewImage> reviewImageEntities = new ArrayList<>();

        if (uploadedFiles != null && !uploadedFiles.isEmpty()) {
            int orderImage = 1;
            for (Map<String, String> imageData : uploadedFiles) {
                ReviewImage reviewImage = ReviewImage.builder()
                        .imageUrl(imageData.get("fileUrl"))
                        .publicImageId(imageData.get("publicId"))
                        .orderImage(orderImage++) // optional field if you track image order
                        .build();
                reviewImageEntities.add(reviewImage);
            }
        }

        log.info(">>> [Review Service] Create Review: Create review entity.");
        Review review = reviewMapper.toEntity(request, reviewImageEntities);
        review.setOrder(order);

        log.info(">>> [Review Service] Create Review: Ended.");
        Review savedReview = reviewRepository.save(review);
        for (ReviewImage i : reviewImageEntities) {
            i.setReview(savedReview);
            reviewImagesRepository.save(i);
        }

        // ✅ Gửi notification và email cho seller
        try {
            Seller seller = order.getPostProduct().getSeller();
            if (seller != null) {
                log.info(">>> [Review Service] Send notification and email to seller: {}", seller.getSellerId());
                
                // Lấy thông tin sản phẩm và đánh giá
                String productTitle = order.getPostProduct().getTitle();
                String ratingStars = "⭐".repeat((int) Math.round(savedReview.getRating()));
                String feedbackText = savedReview.getFeedback() != null && !savedReview.getFeedback().trim().isEmpty() 
                    ? savedReview.getFeedback() 
                    : "Không có phản hồi";

                // Tạo notification
                String notificationTitle = "Đánh giá mới cho sản phẩm của bạn";
                String notificationContent = String.format(
                    "Bạn nhận được đánh giá %.1f/5 sao cho sản phẩm \"%s\"", 
                    savedReview.getRating(), 
                    productTitle
                );
                
                Notification notification = Notification.builder()
                        .receiverId(seller.getSellerId())
                        .type(AccountType.SELLER)
                        .title(notificationTitle)
                        .content(notificationContent)
                        .readAt(null)
                        .build();
                Notification savedNotification = notificationService.createNotification(notification);

                // Gửi real-time notification qua WebSocket
                notificationSocketController.sendNotificationToUser(savedNotification);

                // Gửi email cho seller
                String emailSubject = "Đánh giá mới cho sản phẩm của bạn";
                String emailMessage = String.format("""
                    <div style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto;'>
                        <h2 style='color: #4CAF50;'>💚 Thông báo từ Green Trade Platform</h2>
                        <p>Xin chào <strong>%s</strong>,</p>
                        <p>Sản phẩm <strong>"%s"</strong> của bạn vừa nhận được một đánh giá mới từ khách hàng.</p>
                        <div style='background-color: #f5f5f5; padding: 15px; border-radius: 8px; margin: 20px 0;'>
                            <p style='margin: 5px 0;'><strong>Đánh giá:</strong> %s <strong>(%.1f/5 sao)</strong></p>
                            <p style='margin: 5px 0;'><strong>Phản hồi:</strong></p>
                            <p style='margin: 10px 0; padding: 10px; background-color: white; border-left: 3px solid #4CAF50;'>%s</p>
                        </div>
                        <p>Bạn có thể xem chi tiết đánh giá trong phần quản lý sản phẩm của mình.</p>
                        <p>💚 Cảm ơn bạn đã hợp tác cùng Green Trade Platform!</p>
                    </div>
                    """, 
                    seller.getSellerName() != null ? seller.getSellerName() : seller.getBuyer().getFullName(),
                    productTitle,
                    ratingStars,
                    savedReview.getRating(),
                    feedbackText
                );

                MailRequest mailRequest = MailRequest.builder()
                        .from("green.trade.platform.391@gmail.com")
                        .to(seller.getBuyer().getEmail())
                        .subject(emailSubject)
                        .message(emailMessage)
                        .build();
                mailService.sendBeautifulMail(mailRequest);

                log.info(">>> [Review Service] Notification and email sent successfully to seller: {}", seller.getSellerId());
            }
        } catch (Exception e) {
            log.error(">>> [Review Service] Failed to send notification/email to seller: {}", e.getMessage(), e);
            // Không throw exception để không ảnh hưởng đến việc tạo review
        }

        return savedReview;
    }

    private Map<String, Object> uploadReviewImages(List<MultipartFile> reviewImages) {
        Map<String, Object> response = new HashMap<>();

        if (reviewImages == null || reviewImages.isEmpty()) {
            response.put("message", "Do not have any images.");
            response.put("uploaded", Collections.emptyList());
            response.put("failed", Collections.emptyList());
            return response;
        }

        List<Map<String, String>> uploadedFiles = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();

        for (MultipartFile file : reviewImages) {
            try {
                Map<String, String> uploadResult = cloudinaryService.upload(file, "reviews");
                if (uploadResult != null) {
                    uploadedFiles.add(uploadResult);
                } else {
                    failedFiles.add(file.getOriginalFilename());
                }
            } catch (IOException e) {
                failedFiles.add(file.getOriginalFilename());
            }
        }

        response.put("uploaded", uploadedFiles);
        response.put("failed", failedFiles);
        response.put("totalUploaded", uploadedFiles.size());
        response.put("totalFailed", failedFiles.size());
        response.put("message", "Done upload images to Cloudinary.");

        return response;
    }

    /**
     * Validate rating and feedback fields before saving.
     */
    private Order validateReviewRequest(ReviewRequest request) {
        log.info(">>> [Review Service] Validate Review Request: Started.");
        Order order = orderRepository.findOrderById(request.getOrderId()).orElseThrow(
                () -> new IllegalArgumentException("This order id does not exists.")
        );
        if (request.getRating() < 0 || request.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be in from 0 to 5.");
        }
        log.info(">>> [Review Service] Validate Review Request: Ended.");

        return order;
    }

    public Review getReviewsByOrderId(Long orderId) {
        return reviewRepository.findByOrder_Id(orderId).orElseThrow(
                () -> new IllegalArgumentException("Do not have any review for order id: " + orderId)
        );
    }

    public Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId).orElseThrow(
                () -> new IllegalArgumentException("Review not found with id: " + reviewId)
        );
    }

    public Page<Review> getReviewsBySeller(Seller seller, int page, int size) {
        log.info(">>> [ReviewServiceImpl] getReviewsBySeller - sellerId: {}, page: {}, size: {}", seller.getSellerId(), page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Review> result = reviewRepository.findByOrder_PostProduct_Seller(seller, pageable);
        log.info(">>> [ReviewServiceImpl] getReviewsBySeller - result: {} reviews found", result.getTotalElements());
        return result;
    }

    public Review updateReview(Long reviewId, UpdateReviewRequest request, List<MultipartFile> newImages) {
        log.info(">>> [Review Service] Update Review: Started. ReviewId: {}", reviewId);

        // Tìm review cần update
        Review review = reviewRepository.findById(reviewId).orElseThrow(
                () -> new IllegalArgumentException("Review not found with id: " + reviewId)
        );

        // Validate rating nếu có
        if (request.getRating() != null) {
            if (request.getRating() < 0 || request.getRating() > 5) {
                throw new IllegalArgumentException("Rating must be between 0 and 5.");
            }
            review.setRating(request.getRating());
        }

        // Validate và update feedback nếu có
        if (request.getFeedback() != null && !request.getFeedback().trim().isEmpty()) {
            if (badWordFilter.containsBadWord(request.getFeedback())) {
                throw new IllegalArgumentException("This feedback contains bad words. Please write again.");
            }
            review.setFeedback(request.getFeedback());
        }

        // Xử lý hình ảnh mới nếu có
        if (newImages != null && !newImages.isEmpty()) {
            log.info(">>> [Review Service] Upload new images into Cloudinary: Started.");
            Map<String, Object> uploadResult = uploadReviewImages(newImages);

            List<Map<String, String>> uploadedFiles = (List<Map<String, String>>) uploadResult.get("uploaded");

            if (uploadedFiles != null && !uploadedFiles.isEmpty()) {
                // Xóa các hình ảnh cũ
                if (review.getReviewImages() != null && !review.getReviewImages().isEmpty()) {
                    for (ReviewImage oldImage : review.getReviewImages()) {
                        try {
                            // Xóa hình ảnh cũ từ Cloudinary
                            if (oldImage.getPublicImageId() != null) {
                                cloudinaryService.delete(oldImage.getPublicImageId(), "reviews");
                            }
                        } catch (Exception e) {
                            log.warn(">>> [Review Service] Failed to delete old image from Cloudinary: {}", e.getMessage());
                        }
                        reviewImagesRepository.delete(oldImage);
                    }
                }

                // Thêm hình ảnh mới
                int orderImage = 1;
                for (Map<String, String> imageData : uploadedFiles) {
                    ReviewImage reviewImage = ReviewImage.builder()
                            .imageUrl(imageData.get("fileUrl"))
                            .publicImageId(imageData.get("publicId"))
                            .orderImage(orderImage++)
                            .review(review)
                            .build();
                    reviewImagesRepository.save(reviewImage);
                }
            }
        }

        log.info(">>> [Review Service] Update Review: Ended.");
        return reviewRepository.save(review);
    }
}
