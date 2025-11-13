package Green_trade.green_trade_platform.repository;

import Green_trade.green_trade_platform.enumerate.TransactionType;
import Green_trade.green_trade_platform.model.Order;
import Green_trade.green_trade_platform.model.Wallet;
import Green_trade.green_trade_platform.model.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    boolean existsByExternalTransactionReference(String txnRef);

    Optional<WalletTransaction> findByExternalTransactionReference(String txnRef);

    Page<WalletTransaction> findByWallet(Wallet wallet, Pageable pageable);
    
    // Tìm refund transaction cho buyer từ dispute
    // Query theo description pattern vì order có thể chưa được set khi tạo transaction
    @Query("SELECT wt FROM WalletTransaction wt WHERE wt.type = :type AND wt.description LIKE :descriptionPattern")
    List<WalletTransaction> findRefundTransactionsByTypeAndDescription(
            @Param("type") TransactionType type,
            @Param("descriptionPattern") String descriptionPattern
    );
    
    // Query theo order (nếu order đã được set)
    @Query("SELECT wt FROM WalletTransaction wt WHERE wt.order = :order AND wt.type = :type AND wt.description LIKE :descriptionPattern")
    List<WalletTransaction> findRefundTransactionsByOrderAndType(
            @Param("order") Order order,
            @Param("type") TransactionType type,
            @Param("descriptionPattern") String descriptionPattern
    );
}
