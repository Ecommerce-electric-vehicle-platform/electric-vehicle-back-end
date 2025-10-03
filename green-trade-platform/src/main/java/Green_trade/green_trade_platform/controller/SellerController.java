package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.request.UpgradeRequest;
import Green_trade.green_trade_platform.service.implement.SellerServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("api/v1/seller")
public class SellerController {
    @Autowired
    private SellerServiceImpl sellerServiceImpl;
    @Autowired
    private ResponseMapper responseMapper;

    @Operation(
            summary = "Register a seller",
            description = "Upload all required files (Identity card, Business license, Store policy) at once when registering"
    )
    @PostMapping(
            value = "/{id}/upload-all",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadAll(@PathVariable Long id,
                                       @Valid @ModelAttribute UpgradeRequest request,
                                       @RequestParam(value = "identity", required = true) MultipartFile identityFile,
                                       @RequestParam(value = "business_license", required = true) MultipartFile businessLicenseFile,
                                       @RequestParam(value = "store_policy", required = true) MultipartFile storePolicyFile) throws IOException {
            Map<String, String> result = sellerServiceImpl.uploadSellerDocuments(id, request.getStoreName(),
                    request.getTaxNumber(), request.getIdentityNumber(), identityFile, businessLicenseFile, storePolicyFile);
            return ResponseEntity.ok(responseMapper.toDto(true, "UPLOAD DOCUMENTS SUCCESSFULLY", result, null));
    }
}
