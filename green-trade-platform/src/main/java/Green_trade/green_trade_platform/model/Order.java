package Green_trade.green_trade_platform.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(name = "order_code", nullable = false, unique = true)
    private String orderCode;

    @Column(name = "shipping_address", nullable = false, unique = false)
    private String shippingAddress;

    @Column(name = "phone_number", nullable = false, unique = false)
    private String phoneNumber;

    @Column(name = "price",  nullable = false, unique = false)
    private BigDecimal price;

    @Column(name = "shipping_fee", nullable = false, unique = false)
    private double shippingFee;

    @Column(name = "status", nullable = false, unique = false)
    private String status;

    @Column(name = "created_at", nullable = false, unique = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = true, unique = false)
    private LocalDateTime updatedAt;

    @Column(name = "canceled_at", nullable = true, unique = false)
    private LocalDateTime canceledAt;

    @Column(name = "cancel_reason", nullable = true, unique = false)
    private String cancelReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id")
    @JsonBackReference
    private Buyer buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Admin admin;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews;

    @OneToOne()
    @JoinColumn(name = "post_id", nullable = false, unique = false)
    @JsonManagedReference
    private PostProduct postProduct;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "order")
    @JsonBackReference
    private Invoice invoice;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Dispute> disputes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_partner_id")
    @JsonIgnore
    private ShippingPartner shippingPartner;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
