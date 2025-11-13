package Green_trade.green_trade_platform.service;

import Green_trade.green_trade_platform.model.SystemConfig;

public interface SystemConfigService {
    SystemConfig getConfigByKey(String configKey);

    SystemConfig updateConfig(String configKey, String configValue, Long adminId);

    long getEscrowTransferSeconds();

    long getOrderDeliveredToCompletedSeconds();
}

