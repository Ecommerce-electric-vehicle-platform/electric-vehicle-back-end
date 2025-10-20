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
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    @Column(name = "sender", nullable = false, unique = false)
    private String sender;

    @Column(name = "receiver", nullable = false, unique = false)
    private String receiver;

    @Column(name = "status", nullable = false, unique = false)
    private String status;

    @Column(name = "sent_at", nullable = false, unique = false)
    private LocalDateTime sentAt;

    @Column(name = "read_at", nullable = false, unique = false)
    private LocalDateTime readAt;

    @Column(name = "sender_id", nullable = false, unique = false)
    private Long senderId;

    @Column(name = "content", nullable = false, unique = false)
    private String content;

    @Column(name = "receiver_id", nullable = false, unique = false)
    private Long receiverId;

    @Column(name = "attached_url", nullable = false, unique = false)
    private String attachedUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_type_id")
    private MessageType messageType;

    @PrePersist
    public void onCreate() {
        this.sentAt = LocalDateTime.now();
    }
}
