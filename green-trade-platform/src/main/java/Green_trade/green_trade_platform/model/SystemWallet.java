package Green_trade.green_trade_platform.model;

import Green_trade.green_trade_platform.enumerate.AccountStatus;
import Green_trade.green_trade_platform.enumerate.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "system_wallet")
public class SystemWallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "system_wallet_id")
    private Long id;

    @Column(name = "buyer_wallet_id", nullable = false, unique = true)
    private Long buyerWalletId;

    @Column(name = "buyer_wallet_id", nullable = false, unique = true)
    private Long sellerWalletId;

    @Column(name = "buyer_wallet_id", nullable = false, unique = true)
    private String concurrency;

    @Column(name = "buyer_wallet_id", nullable = false, unique = true)
    private BigDecimal balannce;

    @Column(name = "buyer_wallet_id", nullable = false, unique = true)
    private String status;

    @Column(name = "buyer_wallet_id", nullable = false, unique = true)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
