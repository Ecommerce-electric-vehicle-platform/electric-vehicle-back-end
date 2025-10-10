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
@Table(name = "post_product")
public class PostProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @Column(name = "title", nullable = false, unique = false)
    private String title;

    @Column(name = "brand", nullable = false, unique = false)
    private String brand;

    @Column(name = "model", nullable = false, unique = false)
    private String model;

    @Column(name = "manufacture_year", nullable = false, unique = false)
    private String manufactureYear;

    @Column(name = "used_duration", nullable = false, unique = false)
    public String usedDuration;

    @Column(name = "rejected_reason", nullable = false, unique = false)
    private String rejectedReason;

    @Column(name = "condition_level", nullable = false, unique = false)
    private String conditionLevel;

    @Column(name = "price", nullable = false, unique = false)
    private double price;

    @Column(name = "description", nullable = false, unique = false)
    public String description;

    @Column(name = "location_trading", nullable = false, unique = false)
    private String locationTrading;

    @Column(name = "status", nullable = false, unique = false)
    private boolean status;

    @Column(name = "created_at", nullable = false, unique = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, unique = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", nullable = false, unique = false)
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Admin admin;
}
