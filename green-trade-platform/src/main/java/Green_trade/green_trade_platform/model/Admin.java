package Green_trade.green_trade_platform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false, unique = false)
    private String password;

    @Column(name = "full_name", nullable = false, unique = false)
    private String fullName;

    @Column(name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "is_super_admin", nullable = false, unique = false)
    private String isSuperAdmin;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "status", nullable = false, unique = false)
    private boolean status;

    @Column(name = "created_at", nullable = false, unique = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, unique = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostProduct> postProducts;

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Dispute> disputes;

}
