package Green_trade.green_trade_platform.repository;

import Green_trade.green_trade_platform.model.Following;
import Green_trade.green_trade_platform.model.FollowingId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowingRepository extends JpaRepository<Following, FollowingId> {

    // Tìm following record theo buyer và seller
    Optional<Following> findByBuyer_BuyerIdAndSeller_SellerId(Long buyerId, Long sellerId);

    // Kiểm tra buyer có đang follow seller không (unfollowedAt = null)
    @Query("SELECT f FROM Following f WHERE f.buyer.buyerId = :buyerId AND f.seller.sellerId = :sellerId AND f.unfollowedAt IS NULL")
    Optional<Following> findActiveFollowing(@Param("buyerId") Long buyerId, @Param("sellerId") Long sellerId);

    // Lấy danh sách sellers mà buyer đang follow (unfollowedAt = null)
    @Query("SELECT f FROM Following f WHERE f.buyer.buyerId = :buyerId AND f.unfollowedAt IS NULL")
    Page<Following> findActiveFollowingsByBuyer(@Param("buyerId") Long buyerId, Pageable pageable);

    // Lấy danh sách buyers đang follow seller (unfollowedAt = null)
    @Query("SELECT f FROM Following f WHERE f.seller.sellerId = :sellerId AND f.unfollowedAt IS NULL")
    Page<Following> findActiveFollowersBySeller(@Param("sellerId") Long sellerId, Pageable pageable);

    // Đếm số lượng followers của seller (unfollowedAt = null)
    @Query("SELECT COUNT(f) FROM Following f WHERE f.seller.sellerId = :sellerId AND f.unfollowedAt IS NULL")
    Long countActiveFollowersBySeller(@Param("sellerId") Long sellerId);

    // Đếm số lượng sellers mà buyer đang follow (unfollowedAt = null)
    @Query("SELECT COUNT(f) FROM Following f WHERE f.buyer.buyerId = :buyerId AND f.unfollowedAt IS NULL")
    Long countActiveFollowingsByBuyer(@Param("buyerId") Long buyerId);
}

