package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.mapper.SystemConfigMapper;
import Green_trade.green_trade_platform.model.Admin;
import Green_trade.green_trade_platform.model.SystemConfig;
import Green_trade.green_trade_platform.request.UpdateSystemConfigRequest;
import Green_trade.green_trade_platform.response.SystemConfigResponse;
import Green_trade.green_trade_platform.service.SystemConfigService;
import Green_trade.green_trade_platform.service.implement.AdminServiceImpl;
import Green_trade.green_trade_platform.service.implement.SystemConfigServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/system-config")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "System Config", description = "APIs for managing system configuration (Admin only)")
public class SystemConfigController {
    private final SystemConfigServiceImpl systemConfigService;
    private final ResponseMapper responseMapper;
    private final AdminServiceImpl adminService;
    private final SystemConfigMapper configMapper;

    @Operation(
            summary = "Get system config by key",
            description = "Retrieves a system configuration value by its key. Admin only."
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/{configKey}")
    public ResponseEntity<?> getConfig(@PathVariable String configKey) {
        try {
            Admin admin = adminService.getCurrentUser();
            if(!admin.isSuperAdmin()) {
                throw new IllegalArgumentException("Only super admin can access this resource.");
            }
            SystemConfig config = systemConfigService.getConfigByKey(configKey);
            SystemConfigResponse response = configMapper.toDto(config);
            return ResponseEntity.ok(responseMapper.toDto(true,
                    "Config retrieved successfully.",
                    response,
                    null));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(false,
                    "Failed to retrieve config: " + e.getMessage(),
                    null,
                    e));
        }
    }

    @Operation()
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/all-config")
    public ResponseEntity<?> getAllConfig(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        try {
            Page<SystemConfig> configs = systemConfigService.getAllConfig(page, size);
            Page<SystemConfigResponse> responses = configs.map(configMapper::toDto);
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "Get all system configs successfully.",
                    responses, null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "Get all system configs failed.",
                    null, e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Update system config by key",
            description = "Updates a system configuration value by its key. Admin only."
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{configKey}")
    public ResponseEntity<?> updateConfig(
            @PathVariable String configKey,
            @Valid @RequestBody UpdateSystemConfigRequest request) {
        try {
            Admin admin = adminService.getCurrentUser();
            if(!admin.isSuperAdmin()) {
                throw new IllegalArgumentException("Only super admin can access this resource.");
            }

            SystemConfig updatedConfig = systemConfigService.updateConfig(
                    configKey,
                    request.getConfigValue(),
                    admin.getId()
            );

            SystemConfigResponse response = configMapper.toDto(updatedConfig);

            return ResponseEntity.ok(responseMapper.toDto(true,
                    "Config updated successfully.",
                    response,
                    null));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(false,
                    "Failed to update config: " + e.getMessage(),
                    null,
                    e));
        }
    }

    @Operation(
            summary = "Update escrow transfer seconds",
            description = "Updates the escrow transfer delay in seconds. Admin only. This configures how long before escrow funds are automatically transferred to seller."
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/escrow-transfer-seconds")
    public ResponseEntity<?> updateEscrowTransferSeconds(
            @Valid @RequestBody UpdateSystemConfigRequest request) {
        try {
            Admin admin = adminService.getCurrentUser();
            if(!admin.isSuperAdmin()) {
                throw new IllegalArgumentException("Only super admin can access this resource.");
            }

            String configKey = "ESCROW_TRANSFER_SECONDS";

            SystemConfig updatedConfig = systemConfigService.updateConfig(
                    configKey,
                    request.getConfigValue(),
                    admin.getId()
            );

            long seconds = Long.parseLong(updatedConfig.getConfigValue());
            long days = seconds / 86400;
            long hours = seconds / 3600;
            long minutes = seconds / 60;

            Map<String, Object> result = Map.of(
                    "config", configMapper.toDto(updatedConfig),
                    "converted", Map.of(
                            "seconds", seconds,
                            "days", days,
                            "hours", hours,
                            "minutes", minutes
                    )
            );

            return ResponseEntity.ok(responseMapper.toDto(true,
                    "Escrow transfer seconds updated successfully.",
                    result,
                    null));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(false,
                    "Failed to update escrow transfer seconds: " + e.getMessage(),
                    null,
                    e));
        }
    }

    @Operation(
            summary = "Get escrow transfer seconds config",
            description = "Gets the configured number of seconds before escrow funds are automatically transferred to seller."
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/escrow-transfer-seconds")
    public ResponseEntity<?> getEscrowTransferSeconds() {
        try {
            Admin admin = adminService.getCurrentUser();
            if(!admin.isSuperAdmin()) {
                throw new IllegalArgumentException("Only super admin can access this resource.");
            }

            long seconds = systemConfigService.getEscrowTransferSeconds();
            long days = seconds / 86400;
            Map<String, Object> result = Map.of(
                    "seconds", seconds,
                    "days", days,
                    "hours", seconds / 3600,
                    "minutes", seconds / 60
            );
            return ResponseEntity.ok(responseMapper.toDto(true,
                    "Escrow transfer seconds retrieved successfully.",
                    result,
                    null));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(false,
                    "Failed to retrieve escrow transfer seconds: " + e.getMessage(),
                    null,
                    e));
        }
    }
}

