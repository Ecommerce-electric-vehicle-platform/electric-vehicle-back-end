package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.BuyerMapper;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.Seller;
import Green_trade.green_trade_platform.request.PlaceOrderRequest;
import Green_trade.green_trade_platform.request.ProfileRequest;
import Green_trade.green_trade_platform.request.UpdateBuyerProfileRequest;
import Green_trade.green_trade_platform.response.BuyerResponse;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.service.implement.BuyerServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/buyer")
@Slf4j
public class BuyerController {
    @Autowired
    private BuyerServiceImpl buyerService;
    @Autowired
    private ResponseMapper responseMapper;
    @Autowired
    private BuyerMapper buyerMapper;

    @Operation(
            summary = "Upload buyer profile",
            description = "Upload buyer profile: avatar, full name, shipping address, and so on"
    )
    @PostMapping(
            value = "/{id}/upload-profile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('ROLE_BUYER')")
    public ResponseEntity<?> uploadBuyerProfile(@Parameter(description = "profile request for buyer")
                                                @Valid @ModelAttribute ProfileRequest profileRequest,
                                                @Parameter(description = "avatar of buyer")
                                                @RequestPart(value = "avatar_url", required = true) MultipartFile avatarFile) throws IOException {
        Map<String, Object> body = buyerService.uploadBuyerProfile(profileRequest, avatarFile);
        Buyer tempProfile = (Buyer) body.get("profile");
        return ResponseEntity.ok(responseMapper.toDto(
                true,
                "UPLOAD PROFILE SUCCESS.",
                buyerMapper.toDto(tempProfile),
                null));
    }

    @Operation(summary = "Update Profile Buyer",
                description = "Update buyer profile: buyer profile information")
    @PutMapping(
            value = "/{id}/update-profile",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<RestResponse<BuyerResponse, Object>> updateProfile(
            @Valid @ModelAttribute UpdateBuyerProfileRequest updateProfileRequest,
            @RequestPart(value = "avatarImage") MultipartFile avatarFile
    ) throws Exception {
        log.info(">>> Passed came updateProfile API");
        log.info(">>> updateProfileRequest: {}", updateProfileRequest);
        log.info(">>> avatarFile: {}", avatarFile);

        Buyer buyer = buyerService.updateProfile(updateProfileRequest, avatarFile);
        BuyerResponse responseData = buyerMapper.toDto(buyer);

        return ResponseEntity.status(HttpStatus.OK.value()).body(
                responseMapper.toDto(
                true,
                        "UPDATED PROFILE SUCCESSFULLY",
                        responseData,
                        null
                )
        );
    }

    @Operation(
            summary = "Get buyer profile.",
            description = "This API return user profile. Front-end just pass token into header of request," +
                    " then system will return profile based on token passed."
    )
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            Buyer buyer = buyerService.getCurrentUser();
            return ResponseEntity.ok(responseMapper.toDto(true,
                    "Get user profile successfully.",
                    buyerMapper.toDto(buyer),
                    null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(responseMapper.toDto(false,
                    "Error occur during get user profile.",
                    null, e));
        }
    }




    public ResponseEntity<?> placeOrder(@Valid PlaceOrderRequest request) {

        return ResponseEntity.status(HttpStatus.OK.value()).body(null);
    }
}
