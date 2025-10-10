package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.model.PostProduct;
import Green_trade.green_trade_platform.request.UploadPostProductRequest;
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

import java.util.Map;

@RestController
@RequestMapping("/api/v1/seller")
@Slf4j
public class SellerController {

    private final ResponseMapper responseMapper;

    private final SellerServiceImpl sellerService;

    private final PostProductServiceImpl postProductService;

    public SellerController(
            SellerServiceImpl sellerService,
            ResponseMapper responseMapper,
            PostProductServiceImpl postProductService
    ) {
        this.sellerService = sellerService;
        this.responseMapper = responseMapper;
        this.postProductService = postProductService;
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
    public ResponseEntity<RestResponse<?, ?>> uploadPostProduct(
            @ModelAttribute UploadPostProductRequest request,
            @RequestPart("picture1") MultipartFile picture1,
            @RequestPart("picture2") MultipartFile picture2,
            @RequestPart("picture3") MultipartFile picture3,
            @RequestPart("picture4") MultipartFile picture4,
            @RequestPart("picture5") MultipartFile picture5
            ) throws Exception {
        log.info(">>> Passed came uploadPostProduct");
        Map<String, MultipartFile> files = Map.of(
                "picture1", picture1,
                "picture2", picture2,
                "picture3", picture3,
                "picture4", picture4,
                "picture5", picture5
        );
        log.info(">>> Passed mapped files data: {}", files);
        PostProduct newPostProduct = postProductService.createNewPostProduct(request, files);
        RestResponse<PostProduct, Object> response = responseMapper.toDto(
                true,
                "UPLOADED POST SUCCESSFULLY",
                newPostProduct,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }


}
