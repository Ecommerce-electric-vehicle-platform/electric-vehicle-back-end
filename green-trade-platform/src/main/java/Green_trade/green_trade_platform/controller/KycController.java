package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.request.UpgradeAccountRequest;
import Green_trade.green_trade_platform.response.KycResponse;
import Green_trade.green_trade_platform.service.implement.KycService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/kyc")
@Slf4j
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
            value = "/verify-kyc",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('ROLE_BUYER')")
    public ResponseEntity<?> verifyKyc(
            @ModelAttribute UpgradeAccountRequest request,
            @RequestPart("front of identity")MultipartFile fronOfIdentity,
            @RequestPart("back of identity")MultipartFile backOfIdentity,
            @RequestPart("business license")MultipartFile license,
            @RequestPart("store policy")MultipartFile policy,
            @RequestPart("selfie")MultipartFile selfie
    ) {
        try {
            KycResponse response = kycService.verify(
                    fronOfIdentity,
                    license,
                    selfie,
                    backOfIdentity,
                    policy,
                    request);
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "KYC INFORMATION SUCCESSFULLY.",
                    response, null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "KYC INFORMATION FAILED.",
                    null, e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Extract fields from the FRONT side of a national identity card (ID card)",
            description = "Accepts a multipart image (photo or scan) of the FRONT side of a national identity card. "
                    + "The endpoint sends the image to FPT AI OCR and returns structured fields "
                    + "extracted from the card (name, date of birth, ID number, gender, issuing authority, etc.).\\n"
                    + "Notes:\\n"
                    + " - Provide the image as `multipart/form-data` with field name `file`.\\n"
                    + " - Supported image types: image/jpeg, image/png. Keep file size within server limits.\\n"
                    + " - This endpoint requires Bearer token authentication (JWT)."
    )
    @GetMapping(
            value = "/identity-information",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> getIdentityCardInfo(
            @Parameter(
                    name = "front_of_identity",
                    description = "Front-side ID image (JPEG/PNG)",
                    required = true,
                    schema = @Schema(type = "string", format = "binary")
            )
            @RequestPart("front_of_identity") MultipartFile file) {
        try {
            Map<String, String> data = kycService.callOcrApi(file);
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET IDENTITY CARD INFORMATION SUCCESSFULLY.",
                    data, null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "GET IDENTITY CARD INFORMATION FAILED.",
                    null, e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Update seller information",
            description = "This endpoint allows a buyer to update their KYC information"
    )
    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<?> updateProfile(
            @RequestPart(value = "store_name", required = false) String storeName,
            @RequestPart(value = "business_license", required = false) MultipartFile license,
            @RequestPart(value = "store_policy", required = false) MultipartFile policy
    ) {
        try {
            KycResponse response = kycService.update(
                    storeName,
                    license,
                    policy
            );
            return ResponseEntity.ok(
                    responseMapper.toDto(true, "UPDATED SUCCESSFULLY", response, null)
            );
        } catch (Exception e) {
            log.error("Error updating KYC: ", e);
            return ResponseEntity.internalServerError().body(
                    responseMapper.toDto(false, "UPDATED FAILED", null, e));
        }
    }
}
