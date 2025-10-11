package Green_trade.green_trade_platform.model;

import Green_trade.green_trade_platform.enumerate.SellerStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.C;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "approve_process")
public class ApproveProcess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long approveProcessId;

    @Column(name = "decision")
    @Enumerated(EnumType.STRING)
    private SellerStatus decision;

    @Column(name = "reason")
    private String reason;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @OneToOne
    @JoinColumn(name = "seller_id", nullable = false, unique = true)
    private Seller seller;

    @OneToOne
    @JoinColumn(name = "admin_id")
    private Admin admin;
}
