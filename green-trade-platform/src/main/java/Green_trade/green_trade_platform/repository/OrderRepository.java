package Green_trade.green_trade_platform.repository;

import Green_trade.green_trade_platform.enumerate.OrderStatus;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.Order;
import Green_trade.green_trade_platform.model.Seller;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findAllByBuyer(Buyer buyer, Pageable pageable);


    Optional<Order> findOrderById(Long id);

    Page<Order> findByPostProduct_SellerAndStatus(Seller seller, OrderStatus orderStatus, Pageable pageable);

    Page<Order> findAllByPostProduct_Seller(Seller seller, Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status AND o.postProduct.seller.sellerId = :sellerId")
    int countByStatusAndSeller(@Param("status") OrderStatus status, @Param("sellerId") Long sellerId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.postProduct.seller.sellerId = :sellerId")
    int countBySeller(@Param("sellerId") Long sellerId);

    @Query("SELECT COALESCE(SUM(o.price), 0) FROM Order o WHERE o.status = 'COMPLETED' AND o.postProduct.seller.sellerId = :sellerId")
    BigDecimal getTotalRevenueBySeller(@Param("sellerId") Long sellerId);
}
