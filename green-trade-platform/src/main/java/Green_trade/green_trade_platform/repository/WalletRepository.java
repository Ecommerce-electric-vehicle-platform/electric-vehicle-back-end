package Green_trade.green_trade_platform.repository;

import Green_trade.green_trade_platform.model.Wallet;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    @Modifying
    @Transactional
    @Query("UPDATE Wallet w SET w.balance = :balance WHERE w.buyer.buyerId = :buyerId")
    void addBalance(@Param("buyerId") Long buyerId, @Param("balance") Long balance);
}
