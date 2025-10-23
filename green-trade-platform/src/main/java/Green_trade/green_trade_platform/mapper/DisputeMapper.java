package Green_trade.green_trade_platform.mapper;

import Green_trade.green_trade_platform.model.Dispute;
import Green_trade.green_trade_platform.response.DisputeResponse;
import org.springframework.stereotype.Component;

@Component
public class DisputeMapper {
    public DisputeResponse toDto(Dispute dispute) {
        return DisputeResponse.builder()
                .disputeId(dispute.getId())
                .disputeCategoryId(dispute.getDisputeCategory().getId())
                .disputeCategoryName(dispute.getDisputeCategory().getTitle())
                .description(dispute.getResolution())
                .resolution(dispute.getResolution())
                .decision(dispute.getDecision())
                .status(dispute.getStatus())
                .evidences(dispute.getEvidences())
                .build();
    }
}
