package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.request.ProfileRequest;
import Green_trade.green_trade_platform.service.implement.BuyerServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/buyer")
public class BuyerController {
    @Autowired
    private BuyerServiceImpl buyerService;
    @Autowired
    private ResponseMapper responseMapper;

    @Operation(
            summary = "Upload buyer profile",
            description = "Upload buyer profile: avatar, full name, shipping address, and so on"
    )
    @PostMapping(
            value = "/{id}/upload-profile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('ROLE_BUYER')")
    public ResponseEntity<?> uploadBuyerProfile(@PathVariable Long id,
                                                @Parameter(description = "profile request for buyer")
                                                @Valid @ModelAttribute ProfileRequest profileRequest,
                                                @Parameter(description = "avatar of buyer")
                                                @RequestPart(value = "avatar_url", required = true) MultipartFile avatarFile) throws IOException {
        Map<String, Object> body = buyerService.uploadBuyerProfile(id, profileRequest, avatarFile);
        return ResponseEntity.ok(responseMapper.toDto(true, "UPLOAD PROFILE SUCCESS.",
                body, null));
    }
}
