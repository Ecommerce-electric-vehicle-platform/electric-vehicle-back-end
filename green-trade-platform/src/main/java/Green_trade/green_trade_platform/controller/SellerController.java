package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.request.UpgradeRequest;
import Green_trade.green_trade_platform.service.implement.SellerServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.apache.http.protocol.HTTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
            value = "/{id}/seller-documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadSellerDocument(@PathVariable Long id,
                                       @ModelAttribute UpgradeRequest request,
                                       @RequestParam(value = "identity", required = true) MultipartFile identityFile,
                                       @RequestParam(value = "business_license", required = true) MultipartFile businessLicenseFile,
                                       @RequestParam(value = "store_policy", required = true) MultipartFile storePolicyFile) {
        try {
            Map<String, String> result = sellerServiceImpl.uploadSellerDocuments(id, request.getStoreName(),
                    request.getTaxNumber(), request.getIdentityNumber(), identityFile, businessLicenseFile, storePolicyFile);
            return ResponseEntity.ok(responseMapper.toDto(true, "UPLOAD PROFILE SUCCESSFULLY", result, null));
        } catch (IOException e) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Upload failed: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND,
                    e.getMessage(), e);
        }
    }

    @Operation(summary = "Check if a service package is still valid.",
    description = "Return result of checking service package validity")
    @PostMapping("/{id}/check-service-validity")
    public ResponseEntity<?> checkServiceValidity(@PathVariable Long id) {

        return ResponseEntity.status(HttpStatus.OK.value()).body(null);
    }

    @Operation(summary = "Upload a post",
            description = "Upload a post of product for selling")
    @PostMapping("/products")
    public ResponseEntity<?> uploadPost() throws Exception {
        //check service package still available or not
        //assume it is available
        if(false) {
            throw new Exception("Your Service Package is not available, Extend service to continue");
        }



        return ResponseEntity.status(HttpStatus.OK.value()).body(null);
    }
}
