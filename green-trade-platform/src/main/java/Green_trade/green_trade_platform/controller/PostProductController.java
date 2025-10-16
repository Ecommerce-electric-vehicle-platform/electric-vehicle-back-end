package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.model.PostProduct;
import Green_trade.green_trade_platform.service.implement.PostProductServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/post-product")
@RequiredArgsConstructor
public class PostProductController {
    private final PostProductServiceImpl postProductService;
    private final ResponseMapper responseMapper;

    public ResponseEntity<?> getAllProduct() {
        try {
            int size = 10, page = 0;
            Page<PostProduct> postProductPage = postProductService.getAllPostProduct(page, size);
            return ResponseEntity.ok(responseMapper.toDto(true,
                    "Get post product successfully.",
                    postProductPage, null));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(false,
                    "Get post product failed.",
                    null, e));
        }
    }
}
