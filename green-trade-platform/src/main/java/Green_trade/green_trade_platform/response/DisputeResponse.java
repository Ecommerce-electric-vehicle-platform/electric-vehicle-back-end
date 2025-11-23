package Green_trade.green_trade_platform.response;

import Green_trade.green_trade_platform.enumerate.DisputeDecision;
import Green_trade.green_trade_platform.enumerate.DisputeStatus;
import Green_trade.green_trade_platform.model.Evidence;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisputeResponse {
    private Long disputeId;
    private Long disputeCategoryId;
    private String disputeCategoryName;
    private String description;
    private DisputeDecision decision;
    private String resolution;
    private DisputeStatus status;
    private List<EvidenceResponse> evidences;
    private BigDecimal refundAmount;
    private Double refundPercent;
    private String orderCode;
    private LocalDateTime createdAt;
}
