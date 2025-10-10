package Green_trade.green_trade_platform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(
        name = "buyer",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"username", "deleted_at"})}
)
public class Buyer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long buyerId;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "default_shipping_address")
    private String defaultShippingAddress;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToOne(mappedBy = "buyer")
    private Seller seller;

    @OneToOne(mappedBy = "buyer")
    private Wallet wallet;

    @PrePersist
    public void onCreate() {
        this.isActive = true;
        this.fullName = "Not have yet";
        this.defaultShippingAddress = "Not have yet";
        this.phoneNumber = "Not have yet";
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate(){
        this.updatedAt = LocalDateTime.now();
    }
}
