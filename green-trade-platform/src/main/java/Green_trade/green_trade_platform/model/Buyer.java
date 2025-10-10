package Green_trade.green_trade_platform.model;

import Green_trade.green_trade_platform.enumerate.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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
    private boolean isActive;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dob;

    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @Column(name = "update_at")
    private LocalDateTime updateAt;

    @Column(name = "delete_at")
    private LocalDateTime deleteAt;

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
        this.createAt = LocalDateTime.now();
        this.isActive = true;
    }

    @PreUpdate
    public void onUpdate(){
        this.updateAt = LocalDateTime.now();
    }
}
