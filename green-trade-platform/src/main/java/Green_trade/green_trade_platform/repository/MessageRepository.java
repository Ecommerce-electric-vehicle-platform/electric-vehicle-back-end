package Green_trade.green_trade_platform.repository;

import Green_trade.green_trade_platform.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
