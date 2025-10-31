package Green_trade.green_trade_platform.model;

import Green_trade.green_trade_platform.enumerate.MessageStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Table(name = "message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    @Column(name = "sender_id", nullable = false, unique = false)
    private Long senderId;

    @Column(name = "receiver_id", nullable = false, unique = false)
    private Long receiverId;

    @Column(name = "status", nullable = false, unique = false, length = 50)
    @Enumerated(EnumType.STRING)
    private MessageStatus status;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "content")
    private String content;

    @Column(name = "attached_url")
    private String attachedUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_type_id")
    private MessageType messageType;

    @ManyToOne
    @JoinColumn(name = "conservation")
    @JsonManagedReference
    private Conversation conversation;

    @PrePersist
    public void onCreate() {
        this.sentAt = LocalDateTime.now();
    }
}
