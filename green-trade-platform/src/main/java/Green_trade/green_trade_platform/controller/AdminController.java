package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.model.PostProduct;
import Green_trade.green_trade_platform.model.Seller;
import Green_trade.green_trade_platform.request.ApproveSellerRequest;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.response.SellerResponse;
import Green_trade.green_trade_platform.service.implement.PostProductServiceImpl;
import Green_trade.green_trade_platform.service.implement.SellerServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final SellerServiceImpl sellerService;
    private final PostProductServiceImpl postProductServiceImpl;
    private final ResponseMapper responseMapper;

    public AdminController(SellerServiceImpl sellerService, PostProductServiceImpl postProductServiceImpl, ResponseMapper responseMapper) {
        this.sellerService = sellerService;
        this.postProductServiceImpl = postProductServiceImpl;
        this.responseMapper = responseMapper;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
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

    @PostMapping("/approve-seller")
    public ResponseEntity<?> handlePendingSeller(@RequestBody ApproveSellerRequest request) {
        Seller seller = sellerService.handlePendingSeller(request);
        return ResponseEntity.ok(seller);

    @GetMapping("/review-post-product-seller")
    public ResponseEntity<RestResponse<List<PostProduct>, Object>> getAllPostProductForReview() {
        List<PostProduct> postProducts = postProductServiceImpl.getAllPostProduct();
        RestResponse<List<PostProduct>, Object> response = responseMapper.toDto(
                true,
                "POST PRODUCT LIST",
                postProducts,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }
}
