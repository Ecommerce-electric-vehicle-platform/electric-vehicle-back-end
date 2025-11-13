package Green_trade.green_trade_platform.repository;

import Green_trade.green_trade_platform.model.Subscription;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
//    Optional<Subscription> findBySeller_SellerIdOrderByEndDayDesc(Long sellerId);

    Optional<Subscription> findFirstBySeller_SellerIdOrderByEndDayDesc(Long sellerId);

    Subscription findBySeller_SellerId(Long sellerSellerId);

    @Query("SELECT SUM(s.priceAtPurchase) FROM Subscription s")
    Double getTotalRevenue();

    @Query("SELECT s FROM Subscription s WHERE s.subscriptionPackage.id = :packageId")
    java.util.List<Subscription> findBySubscriptionPackageId(@Param("packageId") Long packageId);

    Page<Subscription> findBySubscriptionPackage_IdOrderByStartDayDesc(Long packageId, Pageable pageable);

    @Query("SELECT SUM(s.priceAtPurchase) FROM Subscription s WHERE s.subscriptionPackage.id = :packageId")
    Double getTotalRevenueByPackageId(@Param("packageId") Long packageId);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.subscriptionPackage.id = :packageId")
    Long countBySubscriptionPackageId(@Param("packageId") Long packageId);
}
