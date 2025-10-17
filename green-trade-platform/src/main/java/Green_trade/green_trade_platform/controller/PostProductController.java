package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.PostProductListMapper;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.model.PostProduct;
import Green_trade.green_trade_platform.response.PostProductListResponse;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.service.implement.PostProductServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/post-product")
@RequiredArgsConstructor
public class PostProductController {
    private final PostProductServiceImpl postProductService;
    private final ResponseMapper responseMapper;
    private final PostProductListMapper postProductListMapper;

    @GetMapping("")
    public ResponseEntity<RestResponse<?, ?>> getAllProduct() {
        try {
            int size = 10, page = 0;
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
            RestResponse<Object, Object> response = responseMapper.toDto(
                    false,
                    "Get post product failed.",
                    null,
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body(response);
        }
    }
}
