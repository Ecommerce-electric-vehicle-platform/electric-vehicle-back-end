package Green_trade.green_trade_platform.repository;

import Green_trade.green_trade_platform.model.Order;
import Green_trade.green_trade_platform.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByOrder(Order order);
}
