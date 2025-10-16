package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.AdminMapper;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.model.Admin;
import Green_trade.green_trade_platform.model.PostProduct;
import Green_trade.green_trade_platform.model.Seller;
import Green_trade.green_trade_platform.request.ApproveSellerRequest;
import Green_trade.green_trade_platform.request.CreateAdminRequest;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.response.SellerResponse;
import Green_trade.green_trade_platform.service.implement.AdminServiceImpl;
import Green_trade.green_trade_platform.service.implement.PostProductServiceImpl;
import Green_trade.green_trade_platform.service.implement.SellerServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final SellerServiceImpl sellerService;
    private final PostProductServiceImpl postProductServiceImpl;
    private final ResponseMapper responseMapper;
    private final AdminServiceImpl adminService;
    private final AdminMapper adminMapper;

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
    }

    @GetMapping("/review-post-product-seller")
    public ResponseEntity<RestResponse<Page<PostProduct>, Object>> getAllPostProductForReview() {
        int page = 0, size = 10;
        Page<PostProduct> postProducts = postProductServiceImpl.getAllPostProduct(page, size);
        RestResponse<Page<PostProduct>, Object> response = responseMapper.toDto(
                true,
                "POST PRODUCT LIST",
                postProducts,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

    @PostMapping("creating-admin")
    public ResponseEntity<?> handleCreatingAdmin(@Valid @ModelAttribute CreateAdminRequest request,
                                                 @RequestPart(value = "avatar_url", required = true)MultipartFile avatarFile) {
       try {
           Admin data = adminService.handleCreateAdminAccount(avatarFile, request);
           return ResponseEntity.ok(responseMapper.toDto(true,
                   "Create admin account successfully.",
                   adminMapper.toDto(data),
                   null));
       } catch (Exception e) {
           return ResponseEntity.ok(responseMapper.toDto(false,
                   "Create admin account failed.",
                   null,
                   e));
       }

    }
}
