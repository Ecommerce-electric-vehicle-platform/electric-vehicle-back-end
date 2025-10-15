package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.PostProductMapper;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.model.PostProduct;
import Green_trade.green_trade_platform.model.Seller;
import Green_trade.green_trade_platform.request.PostProductDecisionRequest;
import Green_trade.green_trade_platform.response.PostProductResponse;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.response.SellerResponse;
import Green_trade.green_trade_platform.service.implement.PostProductServiceImpl;
import Green_trade.green_trade_platform.service.implement.SellerServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final SellerServiceImpl sellerService;
    private final PostProductServiceImpl postProductServiceImpl;
    private final ResponseMapper responseMapper;
    private final PostProductMapper postProductMapper;

    public AdminController(SellerServiceImpl sellerService, PostProductServiceImpl postProductServiceImpl, ResponseMapper responseMapper, PostProductMapper postProductMapper) {
        this.sellerService = sellerService;
        this.postProductServiceImpl = postProductServiceImpl;
        this.responseMapper = responseMapper;
        this.postProductMapper = postProductMapper;
    }

    @PreAuthorize("hasRole('ROLE_BUYER')")
    @GetMapping("")
    public ResponseEntity<?> findAllPendingSeller(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<SellerResponse> ans = sellerService.getAllPendingSeller(page, size);
        Map<String, Object> body = new HashMap<>();
        body.put("sellers", ans.getContent());
        body.put("currentPage", ans.getNumber());
        body.put("totalElements", ans.getTotalElements());
        body.put("totalPage", ans.getTotalPages());

        return ResponseEntity.ok(body);
    }

//    @PostMapping("/approve-decision")
//    public ResponseEntity<?>

    @PreAuthorize("hasRole('ROLE_BUYER')")
    @Operation(summary = "Review Post Product List API",
                    description = "Return a post product list")
    @GetMapping("/review-post-product-seller")
    public ResponseEntity<RestResponse<List<PostProductResponse>, Object>> getAllPostProductForReview() throws Exception {
        List<PostProduct> postProducts = postProductServiceImpl.getAllPostProduct();
        List<PostProductResponse> postProductResponses = new ArrayList<>();

        postProducts.forEach(
            (
                postProduct -> {
                    postProductResponses.add(
                            postProductMapper.toDto(postProduct)
                    );
                }
            )
        );

        RestResponse<List<PostProductResponse>, Object> response = responseMapper.toDto(
                true,
                "POST PRODUCT LIST",
                postProductResponses,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @PreAuthorize("hasRole('ROLE_BUYER')")
    @Operation(summary = "View Post Details For Admin Review API",
                    description = "Return post product detail")
    @GetMapping("/{postProductId}/post-details")
    public ResponseEntity<RestResponse<PostProductResponse, Object>> viewPostProductDetail(
            @PathVariable Long postProductId
        ) throws Exception {
        PostProduct postProduct = postProductServiceImpl.getPostProductById(postProductId);
        PostProductResponse responseData = postProductMapper.toDto(postProduct);
        RestResponse<PostProductResponse, Object> response = responseMapper.toDto(
                true,
                "POST PRODUCT DETAIL",
                responseData,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @Operation(summary = "Decide Post Product API",
                description = "Return a result show that post product decision")
    @PostMapping("/review-post-product-decision")
    public ResponseEntity<RestResponse<?, ?>> reviewPostProductDecision(@Valid PostProductDecisionRequest request) {
        PostProduct result = postProductServiceImpl.decideContentValidation(request);
        return ResponseEntity.status(HttpStatus.OK.value()).body(null);
    }
}
