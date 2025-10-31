package Green_trade.green_trade_platform.repository;

import Green_trade.green_trade_platform.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
}
