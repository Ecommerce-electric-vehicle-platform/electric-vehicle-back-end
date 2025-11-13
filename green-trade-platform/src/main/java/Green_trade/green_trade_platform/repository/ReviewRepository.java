package Green_trade.green_trade_platform.repository;

import Green_trade.green_trade_platform.model.Review;
import Green_trade.green_trade_platform.model.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByOrder_Id(Long orderId);

    Page<Review> findByOrder_PostProduct_Seller(Seller seller, Pageable pageable);
}
