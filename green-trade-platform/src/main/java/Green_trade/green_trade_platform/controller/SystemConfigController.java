package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.filter.BadWordFilter;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.mapper.SystemConfigMapper;
import Green_trade.green_trade_platform.model.Admin;
import Green_trade.green_trade_platform.model.SystemConfig;
import Green_trade.green_trade_platform.request.BadWordConfigRequest;
import Green_trade.green_trade_platform.request.UpdateSystemConfigRequest;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.response.SystemConfigResponse;
import Green_trade.green_trade_platform.service.SystemConfigService;
import Green_trade.green_trade_platform.service.implement.AdminServiceImpl;
import Green_trade.green_trade_platform.service.implement.SystemConfigServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
    private final BadWordFilter badWordFilter;
    private final ObjectMapper objectMapper;

    @Operation(
            summary = "Get system config by key",
            description = """
                    Retrieves a system configuration value by its key. Only super admins can access this endpoint.
                    
                    **Path Parameters:**
                    - `configKey` (String, required): The unique key of the configuration to retrieve
                    
                    **Response:**
                    - Returns the system configuration object with key, value, and metadata
                    
                    **Use Cases:**
                    - Retrieving specific system settings
                    - Configuration lookup by key
                    """,
            tags = {"System Config"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "System configuration retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Super admin required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Configuration key not found"
            )
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/{configKey}")
    public ResponseEntity<?> getConfig(
            @Parameter(description = "Configuration key to retrieve", required = true, example = "MAX_UPLOAD_SIZE")
            @PathVariable String configKey) {
        try {
            Admin admin = adminService.getCurrentUser();
            if (!admin.isSuperAdmin()) {
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

    @Operation(
            summary = "Get all system configurations with pagination",
            description = """
                    Retrieves a paginated list of all system configurations in the system.
                    Only super admins can access this endpoint.
                    
                    **Query Parameters:**
                    - `page` (integer, optional): Page number (0-based index). Default: `0`
                    - `size` (integer, optional): Number of records per page. Default: `10`
                    
                    **Response Structure:**
                    - Paginated list of system configuration objects
                    - Each config includes: key, value, description, updatedBy, updatedAt
                    
                    **Use Cases:**
                    - Admin dashboard viewing all system settings
                    - System configuration management
                    - Monitoring configuration changes
                    """,
            tags = {"System Config"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "System configurations retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Super admin required"
            )
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/all-config")
    public ResponseEntity<?> getAllConfig(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Number of records per page", example = "10")
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
            description = """
                    Updates a system configuration value by its key. Only super admins can update configurations.
                    
                    **Path Parameters:**
                    - `configKey` (String, required): The unique key of the configuration to update
                    
                    **Request Body:**
                    - `configValue` (String, required): New value for the configuration (will be converted to uppercase)
                    
                    **Response:**
                    - Returns the updated system configuration object
                    
                    **Use Cases:**
                    - Updating system settings
                    - Modifying configuration values
                    - System maintenance
                    """,
            tags = {"System Config"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "System configuration updated successfully",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid request data"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Super admin required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Configuration key not found"
            )
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{configKey}")
    public ResponseEntity<?> updateConfig(
            @Parameter(description = "Configuration key to update", required = true, example = "MAX_UPLOAD_SIZE")
            @PathVariable String configKey,
            @Parameter(description = "Update request containing new config value", required = true)
            @Valid @RequestBody UpdateSystemConfigRequest request) {
        try {
            Admin admin = adminService.getCurrentUser();
            if (!admin.isSuperAdmin()) {
                throw new IllegalArgumentException("Only super admin can access this resource.");
            }

            SystemConfig updatedConfig = systemConfigService.updateConfig(
                    configKey,
                    request.getConfigValue().toUpperCase(),
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
            summary = "Get bad words list",
            description = """
                    Retrieves the current list of bad words configured in the system.
                    Only super admins can access this endpoint.
                    
                    **Response:**
                    - Returns the list of bad words as a JSON array
                    
                    **Use Cases:**
                    - Viewing current bad words configuration
                    - Admin dashboard for content filtering management
                    """,
            tags = {"System Config", "Bad Words"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Bad words list retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Super admin required"
            )
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/badwords")
    public ResponseEntity<?> getBadWords() {
        try {
            Admin admin = adminService.getCurrentUser();
            if (!admin.isSuperAdmin()) {
                throw new IllegalArgumentException("Only super admin can access this resource.");
            }

            List<String> badWords = badWordFilter.getBadWords();
            Map<String, Object> data = new HashMap<>();
            data.put("badWords", badWords);
            data.put("count", badWords.size());

            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "Bad words list retrieved successfully.",
                    data,
                    null
            ));
        } catch (Exception e) {
            log.error(">>> [SystemConfigController] Failed to get bad words: {}", e.getMessage());
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "Failed to retrieve bad words: " + e.getMessage(),
                    null,
                    e
            ));
        }
    }

    @Operation(
            summary = "Update bad words list",
            description = """
                    Updates the list of bad words in the system. This will replace the existing list.
                    Only super admins can update this configuration.
                    The bad word filter cache will be automatically refreshed after update.
                    
                    **Request Body:**
                    - `badWords` (List<String>, required): New list of bad words to configure
                    
                    **Response:**
                    - Returns the updated bad words list
                    
                    **Use Cases:**
                    - Adding new bad words to filter
                    - Removing bad words from filter
                    - Updating the entire bad words list
                    """,
            tags = {"System Config", "Bad Words"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Bad words list updated successfully",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid request data"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Super admin required"
            )
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/badwords")
    public ResponseEntity<?> updateBadWords(
            @Parameter(description = "Bad words configuration request", required = true)
            @Valid @RequestBody BadWordConfigRequest request) {
        try {
            Admin admin = adminService.getCurrentUser();
            if (!admin.isSuperAdmin()) {
                throw new IllegalArgumentException("Only super admin can access this resource.");
            }

            // Convert list to JSON string
            String jsonValue = objectMapper.writeValueAsString(request.getBadWords());

            // Update config
            SystemConfig updatedConfig = systemConfigService.updateConfig(
                    "BAD_WORDS",
                    jsonValue,
                    admin.getId()
            );

            Map<String, Object> data = new HashMap<>();
            data.put("badWords", request.getBadWords());
            data.put("count", request.getBadWords().size());

            log.info(">>> [SystemConfigController] Bad words updated by admin {}: {} words", admin.getId(), request.getBadWords().size());

            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "Bad words list updated successfully.",
                    data,
                    null
            ));
        } catch (Exception e) {
            log.error(">>> [SystemConfigController] Failed to update bad words: {}", e.getMessage());
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "Failed to update bad words: " + e.getMessage(),
                    null,
                    e
            ));
        }
    }

    @Operation(
            summary = "Get whitelist words",
            description = """
                    Retrieves the current list of whitelist words (safe words that should not be filtered).
                    Only super admins can access this endpoint.
                    
                    **Response:**
                    - Returns the list of whitelist words as a JSON array
                    
                    **Use Cases:**
                    - Viewing current whitelist configuration
                    - Admin dashboard for content filtering management
                    """,
            tags = {"System Config", "Bad Words"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Whitelist words retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Super admin required"
            )
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/badwords/whitelist")
    public ResponseEntity<?> getWhitelist() {
        try {
            Admin admin = adminService.getCurrentUser();
            if (!admin.isSuperAdmin()) {
                throw new IllegalArgumentException("Only super admin can access this resource.");
            }

            Set<String> whitelist = badWordFilter.getWhitelist();
            Map<String, Object> data = new HashMap<>();
            data.put("whitelist", new ArrayList<>(whitelist));
            data.put("count", whitelist.size());

            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "Whitelist words retrieved successfully.",
                    data,
                    null
            ));
        } catch (Exception e) {
            log.error(">>> [SystemConfigController] Failed to get whitelist: {}", e.getMessage());
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "Failed to retrieve whitelist: " + e.getMessage(),
                    null,
                    e
            ));
        }
    }

    @Operation(
            summary = "Update whitelist words",
            description = """
                    Updates the list of whitelist words (safe words that should not be filtered).
                    Only super admins can update this configuration.
                    The bad word filter cache will be automatically refreshed after update.
                    
                    **Request Body:**
                    - `badWords` (List<String>, required): New list of whitelist words to configure
                    
                    **Response:**
                    - Returns the updated whitelist words
                    
                    **Use Cases:**
                    - Adding new safe words to whitelist
                    - Removing words from whitelist
                    - Updating the entire whitelist
                    """,
            tags = {"System Config", "Bad Words"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Whitelist words updated successfully",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid request data"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Super admin required"
            )
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/badwords/whitelist")
    public ResponseEntity<?> updateWhitelist(
            @Parameter(description = "Whitelist words configuration request", required = true)
            @Valid @RequestBody BadWordConfigRequest request) {
        try {
            Admin admin = adminService.getCurrentUser();
            if (!admin.isSuperAdmin()) {
                throw new IllegalArgumentException("Only super admin can access this resource.");
            }

            // Convert list to JSON string
            String jsonValue = objectMapper.writeValueAsString(request.getBadWords());

            // Update config
            SystemConfig updatedConfig = systemConfigService.updateConfig(
                    "BAD_WORDS_WHITELIST",
                    jsonValue,
                    admin.getId()
            );

            Map<String, Object> data = new HashMap<>();
            data.put("whitelist", request.getBadWords());
            data.put("count", request.getBadWords().size());

            log.info(">>> [SystemConfigController] Whitelist updated by admin {}: {} words", admin.getId(), request.getBadWords().size());

            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "Whitelist words updated successfully.",
                    data,
                    null
            ));
        } catch (Exception e) {
            log.error(">>> [SystemConfigController] Failed to update whitelist: {}", e.getMessage());
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "Failed to update whitelist: " + e.getMessage(),
                    null,
                    e
            ));
        }
    }

}

