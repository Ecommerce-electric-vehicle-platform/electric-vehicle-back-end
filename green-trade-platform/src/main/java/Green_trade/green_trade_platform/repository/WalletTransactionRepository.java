package Green_trade.green_trade_platform.repository;

import Green_trade.green_trade_platform.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
}
