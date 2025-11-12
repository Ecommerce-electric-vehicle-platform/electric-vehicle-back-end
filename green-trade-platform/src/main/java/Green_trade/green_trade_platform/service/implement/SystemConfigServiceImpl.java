package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.model.Admin;
import Green_trade.green_trade_platform.model.SystemConfig;
import Green_trade.green_trade_platform.repository.AdminRepository;
import Green_trade.green_trade_platform.repository.SystemConfigRepository;
import Green_trade.green_trade_platform.response.SystemConfigResponse;
import Green_trade.green_trade_platform.service.SystemConfigService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {
    private final SystemConfigRepository systemConfigRepository;
    private final AdminRepository adminRepository;

    private static final String ESCROW_TRANSFER_SECONDS_KEY = "ESCROW_TRANSFER_SECONDS";
    // Default: 14 ngày = 14 * 24 * 60 * 60 = 1,209,600 giây
    private static final long DEFAULT_ESCROW_TRANSFER_SECONDS = 1209600L;
    
    private static final String ORDER_DELIVERED_TO_COMPLETED_SECONDS_KEY = "ORDER_DELIVERED_TO_COMPLETED_SECONDS";
    // Default: 3 ngày = 3 * 24 * 60 * 60 = 259,200 giây
    private static final long DEFAULT_ORDER_DELIVERED_TO_COMPLETED_SECONDS = 259200L;

    @Override
    public SystemConfig getConfigByKey(String configKey) {
        return systemConfigRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new IllegalArgumentException("Config not found with key: " + configKey));
    }

    @Override
    @Transactional
    public SystemConfig updateConfig(String configKey, String configValue, Long adminId) {
        SystemConfig config = systemConfigRepository.findByConfigKey(configKey)
                .orElseGet(() -> {
                    SystemConfig newConfig = SystemConfig.builder()
                            .configKey(configKey)
                            .configValue(configValue)
                            .build();
                    if (adminId != null) {
                        Admin admin = adminRepository.findById(adminId)
                                .orElseThrow(() -> new IllegalArgumentException("Admin not found with id: " + adminId));
                        newConfig.setAdmin(admin);
                    }
                    return newConfig;
                });

        config.setConfigValue(configValue);
        if (adminId != null && config.getAdmin() == null) {
            Admin admin = adminRepository.findById(adminId)
                    .orElseThrow(() -> new IllegalArgumentException("Admin not found with id: " + adminId));
            config.setAdmin(admin);
        }

        log.info(">>> [SystemConfigService] Updated config {} to value: {}", configKey, configValue);
        return systemConfigRepository.save(config);
    }

    @Override
    public long getEscrowTransferSeconds() {
        try {
            SystemConfig config = systemConfigRepository.findByConfigKey(ESCROW_TRANSFER_SECONDS_KEY)
                    .orElse(null);
            if (config != null) {
                return Long.parseLong(config.getConfigValue());
            }
        } catch (NumberFormatException e) {
            log.warn(">>> [SystemConfigService] Invalid escrow transfer seconds config value, using default: {} seconds ({} days)", 
                    DEFAULT_ESCROW_TRANSFER_SECONDS, DEFAULT_ESCROW_TRANSFER_SECONDS / 86400);
        }
        return DEFAULT_ESCROW_TRANSFER_SECONDS;
    }

    @Override
    public long getOrderDeliveredToCompletedSeconds() {
        try {
            SystemConfig config = systemConfigRepository.findByConfigKey(ORDER_DELIVERED_TO_COMPLETED_SECONDS_KEY)
                    .orElse(null);
            if (config != null) {
                return Long.parseLong(config.getConfigValue());
            }
        } catch (NumberFormatException e) {
            log.warn(">>> [SystemConfigService] Invalid order delivered to completed seconds config value, using default: {} seconds ({} days)", 
                    DEFAULT_ORDER_DELIVERED_TO_COMPLETED_SECONDS, DEFAULT_ORDER_DELIVERED_TO_COMPLETED_SECONDS / 86400);
        }
        return DEFAULT_ORDER_DELIVERED_TO_COMPLETED_SECONDS;
    }

    public Page<SystemConfig> getAllConfig(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return systemConfigRepository.findAll(pageable);
    }
}

