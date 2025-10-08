package Green_trade.green_trade_platform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "post_product")
public class PostProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, unique = false)
    private String title;

    @Column(name = "brand", nullable = false, unique = false)
    private String brand;

    @Column(name = "model", nullable = false, unique = false)
    private String model;

    @Column(name = "year", nullable = false, unique = false)
    private String year;

    @Column(name = "color", nullable = false, unique = false)
    public String color;

    @Column(name = "price", nullable = false, unique = false)
    private double price;

    @Column(name = "used_duration", nullable = false, unique = true)
    private double usedDuration;

    @Column(name = "description", nullable = false, unique = false)
    private String description;

    @Column(name = "condition_level", nullable = false, unique = false)
    private String conditionLevel;

    @Column(name = "location_trading", nullable = false, unique = false)
    private String locationTrading;

    @Column(name = "status", nullable = false, unique = false)
    public String status;

    @Column(name = "approved_by", nullable = false, unique = false)
    private String approvedBy;

    @Column(name = "created_at", nullable = false, unique = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_At", nullable = false, unique = false)
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

}
