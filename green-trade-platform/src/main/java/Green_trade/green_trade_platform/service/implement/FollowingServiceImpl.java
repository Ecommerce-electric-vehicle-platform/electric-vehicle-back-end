package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.Following;
import Green_trade.green_trade_platform.model.FollowingId;
import Green_trade.green_trade_platform.model.Seller;
import Green_trade.green_trade_platform.repository.FollowingRepository;
import Green_trade.green_trade_platform.repository.SellerRepository;
import Green_trade.green_trade_platform.util.DateUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class FollowingServiceImpl {
    private final FollowingRepository followingRepository;
    private final SellerRepository sellerRepository;

    /**
     * Follow a seller
     * Nếu đã follow rồi (unfollowedAt != null), set lại unfollowedAt = null
     * Nếu chưa follow, tạo mới Following record
     */
    @Transactional
    public Following followSeller(Buyer buyer, Long sellerId) {
        log.info(">>> [Following Service] Follow seller: buyerId: {}, sellerId: {}", buyer.getBuyerId(), sellerId);
        
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found with id: " + sellerId));
        
        // Kiểm tra xem buyer có phải là chính seller này không
        if (buyer.getBuyerId().equals(seller.getBuyer().getBuyerId())) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }
        
        // Tìm following record (nếu có)
        FollowingId followingId = FollowingId.builder()
                .buyerId(buyer.getBuyerId())
                .sellerId(sellerId)
                .build();
        
        Optional<Following> existingFollowing = followingRepository.findById(followingId);
        
        Following following;
        LocalDateTime currentTime = DateUtils.getCurrentVietnamTime();
        
        if (existingFollowing.isPresent()) {
            // Nếu đã có record, check xem đã unfollow chưa
            following = existingFollowing.get();
            if (following.getUnfollowedAt() != null) {
                // Đã unfollow trước đó, set lại để follow
                following.setUnfollowedAt(null);
                following.setFollowedAt(currentTime);
                log.info(">>> [Following Service] Re-follow seller: buyerId: {}, sellerId: {}", buyer.getBuyerId(), sellerId);
            } else {
                // Đã đang follow rồi
                log.info(">>> [Following Service] Already following seller: buyerId: {}, sellerId: {}", buyer.getBuyerId(), sellerId);
                throw new IllegalArgumentException("Already following this seller");
            }
        } else {
            // Tạo mới following record
            following = Following.builder()
                    .id(followingId)
                    .buyer(buyer)
                    .seller(seller)
                    .followedAt(currentTime)
                    .unfollowedAt(null)
                    .build();
            log.info(">>> [Following Service] New follow: buyerId: {}, sellerId: {}", buyer.getBuyerId(), sellerId);
        }
        
        return followingRepository.save(following);
    }

    /**
     * Unfollow a seller
     * Set unfollowedAt = current time (soft delete)
     */
    @Transactional
    public void unfollowSeller(Buyer buyer, Long sellerId) {
        log.info(">>> [Following Service] Unfollow seller: buyerId: {}, sellerId: {}", buyer.getBuyerId(), sellerId);
        
        Following following = followingRepository.findActiveFollowing(buyer.getBuyerId(), sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Not following this seller"));
        
        following.setUnfollowedAt(DateUtils.getCurrentVietnamTime());
        followingRepository.save(following);
        
        log.info(">>> [Following Service] Unfollowed seller successfully: buyerId: {}, sellerId: {}", buyer.getBuyerId(), sellerId);
    }

    /**
     * Check if buyer is following seller
     */
    public boolean isFollowing(Buyer buyer, Long sellerId) {
        return followingRepository.findActiveFollowing(buyer.getBuyerId(), sellerId).isPresent();
    }

    /**
     * Get list of sellers that buyer is following (unfollowedAt = null)
     */
    public Page<Following> getFollowingsByBuyer(Buyer buyer, int page, int size) {
        log.info(">>> [Following Service] Get followings by buyer: buyerId: {}, page: {}, size: {}", buyer.getBuyerId(), page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("followedAt").descending());
        return followingRepository.findActiveFollowingsByBuyer(buyer.getBuyerId(), pageable);
    }

    /**
     * Get list of buyers following a seller (unfollowedAt = null)
     */
    public Page<Following> getFollowersBySeller(Long sellerId, int page, int size) {
        log.info(">>> [Following Service] Get followers by seller: sellerId: {}, page: {}, size: {}", sellerId, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("followedAt").descending());
        return followingRepository.findActiveFollowersBySeller(sellerId, pageable);
    }

    /**
     * Count number of followers for a seller
     */
    public Long countFollowersBySeller(Long sellerId) {
        return followingRepository.countActiveFollowersBySeller(sellerId);
    }

    /**
     * Count number of sellers that buyer is following
     */
    public Long countFollowingsByBuyer(Buyer buyer) {
        return followingRepository.countActiveFollowingsByBuyer(buyer.getBuyerId());
    }
}

