package Green_trade.green_trade_platform.repository;

import Green_trade.green_trade_platform.enumerate.DisputeStatus;
import Green_trade.green_trade_platform.model.Dispute;
import Green_trade.green_trade_platform.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {
    Page<Dispute> findAllByStatus(DisputeStatus disputeStatus, Pageable pageable);

    Optional<Order> findOrderById(Long disputeId);

    List<Dispute> findByOrder_Id(Long orderId);
    
    // Lấy tất cả disputes của một buyer (thông qua order)
    Page<Dispute> findByOrder_Buyer_BuyerId(Long buyerId, Pageable pageable);
    
    // Tìm disputes có status PENDING cho một order
    List<Dispute> findByOrder_IdAndStatus(Long orderId, DisputeStatus status);
}
