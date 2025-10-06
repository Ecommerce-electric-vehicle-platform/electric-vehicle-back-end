package Green_trade.green_trade_platform.model;

import Green_trade.green_trade_platform.enumerate.SellerStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table( name = "seller")
public class Seller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sellerId;

    @Column(name = "identity_image_url", nullable = false, unique = true)
    private String identityImageUrl;

    @Column(name = "business_license_url", nullable = false, unique = true)
    private String businessLicenseUrl;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private SellerStatus status;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Column(name = "store_policy_url", nullable = false, unique = true)
    private String storePolicyUrl;

    @Column(name = "tax_number", unique = true, nullable = false)
    private String taxNumber;

    @Column(name = "identity_number", unique = true, nullable = false)
    private String identityNumber;

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "delete_at")
    private LocalDateTime deleteAt;

    @OneToOne()
    @JoinColumn(name = "buyer_id", nullable = false, unique = true)
    private Buyer buyer;

    @PrePersist
    public void onCreate() {
        this.status = SellerStatus.PENDING;
        this.createAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updateAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subscription> subscriptions;

}
