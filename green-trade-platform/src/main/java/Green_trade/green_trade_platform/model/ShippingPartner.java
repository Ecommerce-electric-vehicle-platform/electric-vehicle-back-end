package Green_trade.green_trade_platform.model;

import Green_trade.green_trade_platform.enumerate.SellerStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "shipping_partner")
public class ShippingPartner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "partner_name", nullable = false, unique = true)
    private String partnerName;

    @Column(name = "address", nullable = false, unique = true)
    private String address;

    @Column(name = "website_url", nullable = false, unique = true)
    private String websiteUrl;

    @Column(name = "hotline", nullable = false, unique = true)
    private String hotline;

    @Column(name = "created_at", nullable = false, unique = true)
    private LocalDateTime createdAt;

    @Column(name = "updatedat", nullable = false, unique = true)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "shippingPartner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
