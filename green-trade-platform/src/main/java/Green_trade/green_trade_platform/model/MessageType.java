package Green_trade.green_trade_platform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "message_type")
public class MessageType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_type_id")
    private Long id;

    @Column(name = "status", nullable = false, unique = false)
    private String status;

    @Column(name = "created_at", nullable = false, unique = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "messageType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
