package Green_trade.green_trade_platform.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Table(name = "following")
public class Following {
    @EmbeddedId
    private FollowingId id;

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    @MapsId("buyerId")
    @JsonManagedReference
    private Buyer buyer;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    @JoinColumn(name = "seller_id")
    @JsonManagedReference
    private Seller seller;

    @Column(name = "followed_at", nullable = false, unique = false)
    private LocalDateTime followedAt;

    @Column(name = "unfollowed_at", nullable = false, unique = false)
    private LocalDateTime unfollowedAt;
}
