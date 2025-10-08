package Green_trade.green_trade_platform.model;

import Green_trade.green_trade_platform.enumerate.WalletConcurrency;
import Green_trade.green_trade_platform.enumerate.WalletStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wallet")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long walletId;

    @Column(name = "balance")
    private BigDecimal balance;

    @Column(name = "concurrency")
    @Enumerated(EnumType.STRING)
    private WalletConcurrency concurrency;

    @Column(name = "provider")
    private String provider;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private WalletStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToOne()
    @JoinColumn(name = "buyer_id")
    private Buyer buyer;

    @OneToMany(mappedBy = "wallet", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    List<WalletTransaction> transactions;

    @PrePersist
    public void onCreate() {
        this.balance = BigDecimal.ZERO;
        this.provider = "PAYOS";
        this.status = WalletStatus.UNPROVISIONED;
        this.createdAt = LocalDateTime.now();
    }

}
