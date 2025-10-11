package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.PostProductMapper;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.model.PostProduct;
import Green_trade.green_trade_platform.request.UploadPostProductRequest;
import Green_trade.green_trade_platform.response.PostProductResponse;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.response.SubscriptionResponse;
import Green_trade.green_trade_platform.service.implement.PostProductServiceImpl;
import Green_trade.green_trade_platform.service.implement.SellerServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/seller")
@Slf4j
public class SellerController {

    private final ResponseMapper responseMapper;

    private final SellerServiceImpl sellerService;

    private final PostProductServiceImpl postProductService;
    private final PostProductMapper postProductMapper;

    public SellerController(
            SellerServiceImpl sellerService,
            ResponseMapper responseMapper,
            PostProductServiceImpl postProductService,
            PostProductMapper postProductMapper) {
        this.sellerService = sellerService;
        this.responseMapper = responseMapper;
        this.postProductService = postProductService;
        this.postProductMapper = postProductMapper;
    }

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


}
