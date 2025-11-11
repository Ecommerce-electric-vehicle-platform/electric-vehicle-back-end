package Green_trade.green_trade_platform.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SystemConfigResponse {
    private long id;
    private String configKey;
    private String configValue;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
