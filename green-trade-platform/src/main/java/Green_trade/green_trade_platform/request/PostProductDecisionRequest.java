package Green_trade.green_trade_platform.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostProductDecisionRequest {
    private String employeeNumber;
    private Long postProductId;
    private boolean passed;
    private String rejectedReason;
}
