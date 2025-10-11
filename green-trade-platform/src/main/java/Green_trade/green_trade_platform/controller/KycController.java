package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.request.UpgradeRequest;
import Green_trade.green_trade_platform.response.KycResponse;
import Green_trade.green_trade_platform.service.implement.KycService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/kyc")
public class KycController {

    private final KycService kycService;
    private final ResponseMapper responseMapper;

    public KycController (KycService kycService, ResponseMapper responseMapper) {
        this.kycService = kycService;
        this.responseMapper = responseMapper;
    }

    @Operation(
            summary = "Upload buyer profile",
            description = "Upload buyer profile: avatar, full name, shipping address, and so on"
    )
    @PostMapping(
            value = "/{userId}/verify-kyc",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('ROLE_BUYER')")
    public ResponseEntity<?> verifyKyc(
            @PathVariable Long userId,
            @ModelAttribute UpgradeRequest request,
            @RequestPart("front of identity")MultipartFile fronOfIdentity,
            @RequestPart("back of identity")MultipartFile backOfIdentity,
            @RequestPart("business license")MultipartFile license,
            @RequestPart("store policy")MultipartFile policy,
            @RequestPart("selfie")MultipartFile selfie
    ) {
        try {
            KycResponse response = kycService.verify(
                    userId,
                    fronOfIdentity,
                    license,
                    selfie,
                    backOfIdentity,
                    policy,
                    request);
            return ResponseEntity.status(HttpStatus.OK.value()).body(
                    responseMapper.toDto(
                            true,
                            "VERIFIED KYC SUCCESSFULLY",
                            response,
                            null
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("KYC verification failed: " + e.getMessage());
        }
    }
}
