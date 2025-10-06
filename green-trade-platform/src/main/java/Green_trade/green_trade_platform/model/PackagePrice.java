package Green_trade.green_trade_platform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "package_price")
public class PackagePrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "price", nullable = false, unique = false)
    private double price;

    @Column(name = "is_active", nullable = false, unique = false)
    private boolean isActive;

    @Column(name = "duration_by_day", nullable = false, unique = false)
    private Long durationByDay;

    @Column(name = "currency", nullable = false, unique = false)
    private String currency;

    @Column(name = "discount_percent", nullable = false, unique = false)
    private double discountPercent;

    @Column(name = "created_at", nullable = false, unique = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, unique = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", nullable = false, unique = false)
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private SubscriptionPackages subscriptionPackage;
}
