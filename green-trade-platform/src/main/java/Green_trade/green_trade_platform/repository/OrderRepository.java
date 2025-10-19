package Green_trade.green_trade_platform.repository;

import Green_trade.green_trade_platform.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
