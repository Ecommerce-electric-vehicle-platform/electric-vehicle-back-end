package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.PostProductMapper;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.mapper.SellerMapper;
import Green_trade.green_trade_platform.model.PostProduct;
import Green_trade.green_trade_platform.model.Seller;
import Green_trade.green_trade_platform.request.UploadPostProductRequest;
import Green_trade.green_trade_platform.request.VerifiedPostProductRequest;
import Green_trade.green_trade_platform.response.PostProductResponse;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.response.SubscriptionResponse;
import Green_trade.green_trade_platform.service.implement.PostProductServiceImpl;
import Green_trade.green_trade_platform.service.implement.SellerServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seller")
@Slf4j
@RequiredArgsConstructor
public class SellerController {

    private final ResponseMapper responseMapper;
    private final SellerServiceImpl sellerService;
    private final SellerMapper sellerMapper;
    private final PostProductServiceImpl postProductService;
    private final PostProductMapper postProductMapper;

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @Operation(summary = "Verify Service Package Validity",
                description = "Return a result to verify that service package is valid")
    @PostMapping("/{username}/check-service-package-validity")
    public ResponseEntity<RestResponse<SubscriptionResponse, Object>> checkServicePackageValidity(@PathVariable Long id) throws Exception {
        SubscriptionResponse result = sellerService.checkServicePackageValidity(id);
        RestResponse<SubscriptionResponse, Object> response = responseMapper.toDto(
                true,
                "Service Package is valid",
                result,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @Operation(summary = "Upload Post For Selling Product Of Seller",
                description = "Return result of uploading products")
    @PostMapping("/post-products")
    public ResponseEntity<RestResponse<PostProductResponse, Object>> uploadPostProduct(
            @ModelAttribute UploadPostProductRequest request,
            @RequestPart("pictures") List<MultipartFile> files
            ) throws Exception {
        log.info(">>> Passed came uploadPostProduct");
        log.info(">>> Passed mapped files data: {}", files);
        PostProduct newPostProduct = postProductService.createNewPostProduct(request, files);
        PostProductResponse responseData = postProductMapper.toDto(newPostProduct);
        RestResponse<PostProductResponse, Object> response = responseMapper.toDto(
                true,
                "UPLOADED POST SUCCESSFULLY",
                responseData,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @PreAuthorize("hasRole('ROLER_SELLER')")
    @Operation(summary = "Request verified for post product",
                description = "Retrun result that the request has been sent")
    @PostMapping("/verified-post-product-request")
    public ResponseEntity<RestResponse<PostProductResponse, Object>> postProductVerifiedRequest(@Valid @RequestBody VerifiedPostProductRequest request) throws Exception {
        PostProduct result = postProductService.postProductVerifiedRequest(request);
        PostProductResponse responseData = postProductMapper.toDto(result);
        RestResponse<PostProductResponse, Object> response = responseMapper.toDto(
                true,
                "VERIFIED POST REQUEST SENT",
                responseData,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @Operation(
            description = "Front-end just pass token then will get seller profile (if seller exists).",
            summary = "Get seller profile."
    )
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            Seller seller = sellerService.getCurrentUser();
            return ResponseEntity.ok(responseMapper.toDto(true,
                    "Get seller profile successfully.",
                    sellerMapper.toDto(seller), null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(responseMapper.toDto(false,
                    "Error occured during get seller profile,",
                    null, e));
        }
    }


}
