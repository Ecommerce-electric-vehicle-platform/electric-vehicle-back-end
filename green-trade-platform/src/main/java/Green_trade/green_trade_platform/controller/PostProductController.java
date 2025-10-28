package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.exception.PostProductNotFound;
import Green_trade.green_trade_platform.mapper.PostProductListMapper;
import Green_trade.green_trade_platform.mapper.PostProductMapper;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.mapper.SellerMapper;
import Green_trade.green_trade_platform.model.PostProduct;
import Green_trade.green_trade_platform.model.Seller;
import Green_trade.green_trade_platform.response.PostProductListResponse;
import Green_trade.green_trade_platform.response.PostProductResponse;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.response.SellerResponse;
import Green_trade.green_trade_platform.service.implement.PostProductServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/post-product")
@RequiredArgsConstructor
public class PostProductController {
    private final PostProductServiceImpl postProductService;
    private final ResponseMapper responseMapper;
    private final PostProductListMapper postProductListMapper;
    private final PostProductMapper postProductMapper;
    private final SellerMapper sellerMapper;

    @Operation(
            summary = "Get all post product with pagination",
            description = "When buyer click to one page then FE will send page, size to BE." +
                    "Size can be default 10 or more."
    )
    @GetMapping("")
    public ResponseEntity<RestResponse<PostProductListResponse, Object>> getAllProduct(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        try {
            Page<PostProduct> postProductPage = postProductService.getAllProductPaging(page, size);
            Map<String, Object> meta = Map.of(
                    "currentPage", postProductPage.getNumber(),
                    "totalElements", postProductPage.getTotalElements(),
                    "totalPage", postProductPage.getTotalPages()
            );

            PostProductListResponse responseData = postProductListMapper.toDto(postProductPage.getContent(), meta);

            RestResponse<PostProductListResponse, Object> response = responseMapper.toDto(
                    true,
                    "Get post product successfully.",
                    responseData,
                    null
            );
            return ResponseEntity.status(HttpStatus.OK.value()).body(response);
        } catch (Exception e) {
            PostProductListResponse responseData = postProductListMapper.toDto(new ArrayList<PostProduct>(), Map.of());
            RestResponse<PostProductListResponse, Object> response = responseMapper.toDto(
                    false,
                    "Get post product failed.",
                    responseData,
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(response);
        }
    }

    @GetMapping("/{postId}/seller")
    public ResponseEntity<RestResponse<SellerResponse, Object>> getSellerByPostId(@PathVariable Long id) {
        PostProduct postProduct = postProductService.findPostProductById(id);
        if(postProduct == null) {
            throw new PostProductNotFound();
        }
        Seller seller = postProduct.getSeller();
        SellerResponse responseData = sellerMapper.toDto(seller);
        RestResponse<SellerResponse, Object> response = responseMapper.toDto(
                true,
                "FETCH SELLER BY POST SUCCESSFULLY",
                responseData,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }
}
