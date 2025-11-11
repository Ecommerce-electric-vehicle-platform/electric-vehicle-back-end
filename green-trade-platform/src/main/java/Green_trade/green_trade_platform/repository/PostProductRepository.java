package Green_trade.green_trade_platform.repository;

import Green_trade.green_trade_platform.enumerate.VerifiedDecisionStatus;
import Green_trade.green_trade_platform.model.PostProduct;
import Green_trade.green_trade_platform.model.Seller;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PostProductRepository extends JpaRepository<PostProduct, Long> {
    Page<PostProduct> findBySeller(Seller seller, Pageable pageable);

    Page<PostProduct> findAllBySoldFalse(Pageable pageable);

    Page<PostProduct> findAllBySoldFalseAndActiveTrue(Pageable pageable);

    Page<PostProduct> findAllByVerifiedDecisionstatus(VerifiedDecisionStatus status, Pageable pageable);

    List<PostProduct> findAllBySeller(Seller seller);

    Page<PostProduct> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<PostProduct> findByBrandContainingIgnoreCase(String brand, Pageable pageable);

    Page<PostProduct> findByModelContainingIgnoreCase(String model, Pageable pageable);

    Page<PostProduct> findByConditionLevelContainingIgnoreCase(String conditionLevel, Pageable pageable);

    Page<PostProduct> findByLocationTradingContainingIgnoreCase(String locationTrading, Pageable pageable);

    @Query("""
                SELECT COUNT(p)
                FROM PostProduct p
                WHERE p.createdAt BETWEEN :startDate AND :endDate
                  AND p.deletedAt IS NULL
            """)
    Long countNewPostsInMonth(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(p) FROM PostProduct p WHERE p.active = :isActive AND p.seller.sellerId = :sellerId")
    int countByActiveAndSeller(@Param("isActive") boolean isActive, @Param("sellerId") Long sellerId);
}
