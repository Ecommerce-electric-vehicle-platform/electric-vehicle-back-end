package Green_trade.green_trade_platform.mapper;

import Green_trade.green_trade_platform.model.SystemConfig;
import Green_trade.green_trade_platform.response.SystemConfigResponse;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
public class SystemConfigMapper {

    public SystemConfigResponse toDto(SystemConfig config) {
        return SystemConfigResponse.builder()
                .id(config.getId())
                .configKey(config.getConfigKey())
                .configValue(config.getConfigValue())
                .description(config.getDescription())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }
}
