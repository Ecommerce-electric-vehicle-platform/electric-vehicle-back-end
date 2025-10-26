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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    @Operation(
            summary = "Verify Service Package Validity",
            description = """
                This endpoint allows a **seller** to verify whether their current service package is still valid.
                <br><br>
                Workflow:
                <ul>
                    <li>Checks the service subscription associated with the seller's username.</li>
                    <li>Returns whether the package is valid and the expiry date.</li>
                </ul>
                """,
            parameters = {
                    @Parameter(
                            name = "username",
                            description = "Username of the seller whose service package needs to be verified",
                            required = true,
                            example = "viennehaha"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Service package validity checked successfully",
                            content = @Content(
                                    schema = @Schema(implementation = RestResponse.class),
                                    examples = @ExampleObject(
                                            name = "Valid Service Package",
                                            value = """
                                                {
                                                  "success": true,
                                                  "message": "Service Package is valid",
                                                  "data": {
                                                    "valid": true,
                                                    "expiryDate": "2025-12-31T23:59:59",
                                                    "packageName": "Premium Seller Plan"
                                                  },
                                                  "error": null
                                                }
                                                """
                                    )
                            )
                    )
            },
            tags = {"Seller Management"}
    )
    @PostMapping("/{username}/check-service-package-validity")
    public ResponseEntity<RestResponse<SubscriptionResponse, Object>> checkServicePackageValidity(@PathVariable String username) throws Exception {
        SubscriptionResponse result = sellerService.checkServicePackageValidity(username);
        RestResponse<SubscriptionResponse, Object> response = responseMapper.toDto(
                true,
                "Service Package is valid",
                result,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @Operation(
            summary = "Upload a product post for selling",
            description = """
                This endpoint allows a **seller** to upload a new post for a product they want to sell.
                <br><br>
                The request consists of:
                <ul>
                    <li>Product details (title, brand, model, price, etc.) in form-data.</li>
                    <li>One or more product images uploaded as multipart files.</li>
                </ul>
                The response returns the created post details after saving it successfully.
                """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Product post data and uploaded images",
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = UploadPostProductRequest.class),
                            examples = @ExampleObject(
                                    name = "Example Request",
                                    value = """
                                        {
                                          "sellerId": 5,
                                          "title": "Used Electric Bike",
                                          "brand": "Yadea",
                                          "model": "X5",
                                          "manufactureYear": 2022,
                                          "usedDuration": "6 months",
                                          "conditionLevel": "Good",
                                          "price": 850.00,
                                          "length": "150",
                                          "width": "60",
                                          "height": "110",
                                          "weight": "25000",
                                          "description": "Lightly used electric bike in perfect condition.",
                                          "locationTrading": "Ho Chi Minh City",
                                          "categoryId": 3
                                        }
                                        """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Product post uploaded successfully",
                            content = @Content(
                                    schema = @Schema(implementation = RestResponse.class),
                                    examples = @ExampleObject(
                                            name = "Success Response",
                                            value = """
                                                {
                                                  "success": true,
                                                  "message": "UPLOADED POST SUCCESSFULLY",
                                                  "data": {
                                                    "postId": 101,
                                                    "sellerId": 5,
                                                    "sellerStoreName": "EcoRider Shop",
                                                    "title": "Used Electric Bike",
                                                    "brand": "Yadea",
                                                    "model": "X5",
                                                    "manufactureYear": 2022,
                                                    "usedDuration": "6 months",
                                                    "conditionLevel": "Good",
                                                    "verifiedDecisionStatus": "PENDING",
                                                    "verified": false,
                                                    "active": true,
                                                    "categoryName": "Electric Vehicles",
                                                    "price": 850.00,
                                                    "locationTrading": "Ho Chi Minh City"
                                                  }
                                                }
                                                """
                                    )
                            )
                    )
            },
            tags = {"Seller Management"}
    )
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

    @PostMapping("/upload-pictures-cloudinary/{id}")
    public ResponseEntity<RestResponse<PostProductResponse, Object>> uploadPostProduct(
            @PathVariable Long id,
            @RequestPart("pictures") List<MultipartFile> files
    ) throws Exception {
        log.info(">>> Passed came uploadPostProduct");
        log.info(">>> Passed mapped files data: {}", files);
        PostProduct newPostProduct = postProductService.uploadPostProductPicture(id, files);
        PostProductResponse responseData = postProductMapper.toDto(newPostProduct);
        RestResponse<PostProductResponse, Object> response = responseMapper.toDto(
                true,
                "UPLOADED POST PICTURES SUCCESSFULLY",
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
                    null, e.getMessage()));
        }
    }




}
