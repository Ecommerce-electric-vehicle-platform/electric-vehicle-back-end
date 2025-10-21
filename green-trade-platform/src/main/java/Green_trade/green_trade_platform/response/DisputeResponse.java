package Green_trade.green_trade_platform.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisputeResponse {
    private Long disputeId;
    private Long disputeCategoryId;
    private String disputeCategoryName;
    private String description;
    private String decision;
    private String resolution;
    private String status;
}
