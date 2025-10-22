package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.AdminMapper;
import Green_trade.green_trade_platform.mapper.PostProductListMapper;
import Green_trade.green_trade_platform.mapper.PostProductMapper;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.model.Admin;
import Green_trade.green_trade_platform.model.Notification;
import Green_trade.green_trade_platform.model.PostProduct;
import Green_trade.green_trade_platform.model.Seller;
import Green_trade.green_trade_platform.request.ApproveSellerRequest;
import Green_trade.green_trade_platform.request.CreateAdminRequest;
import Green_trade.green_trade_platform.request.NeedVerifyPostRequest;
import Green_trade.green_trade_platform.request.PostProductDecisionRequest;
import Green_trade.green_trade_platform.response.PostProductListResponse;
import Green_trade.green_trade_platform.response.PostProductResponse;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.response.SellerResponse;
import Green_trade.green_trade_platform.service.implement.AdminServiceImpl;
import Green_trade.green_trade_platform.service.implement.PostProductServiceImpl;
import Green_trade.green_trade_platform.service.implement.SellerServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    private final SellerServiceImpl sellerService;
    private final PostProductServiceImpl postProductServiceImpl;
    private final ResponseMapper responseMapper;
    private final AdminServiceImpl adminService;
    private final AdminMapper adminMapper;
    private final PostProductMapper postProductMapper;
    private final PostProductListMapper postProductListMapper;
    private final NotificationSocketController socketController;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/pending-seller")
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
    public ResponseEntity<RestResponse<?, ?>> handlePendingSeller(@RequestBody ApproveSellerRequest request) throws JsonProcessingException {
        Notification sellerNotification = sellerService.handlePendingSeller(request);
        sellerNotification.setSendAt(LocalDateTime.now());
        socketController.sendNotificationToUser(sellerNotification);
        return ResponseEntity.ok(responseMapper.toDto(true,
                "Approve request was be solved.",
                sellerNotification, null));
    }

    @PostMapping("creating-admin")
    public ResponseEntity<?> handleCreatingAdmin(
            @Valid @ModelAttribute CreateAdminRequest request,
            @RequestPart(value = "avatar_url", required = true)MultipartFile avatarFile
    ) {
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

//    @PreAuthorize("hasRole('ROLE_SELLER')")
    @Operation(summary = "Review Post Product List API",
            description = "Return a post product list")
    @GetMapping("/review-post-seller-list")
    public ResponseEntity<RestResponse<PostProductListResponse, Object>> getAllPostProductForReview(
            @RequestParam(name = "size",defaultValue = "10") int size,
            @RequestParam(name = "page",defaultValue = "0") int page
    ) throws Exception {
        log.info(">>> Server came getAllPostProductForReview API");
        Page<PostProduct> postProducts = postProductServiceImpl.getAllPostProductForVerifiedReview(size, page);
        log.info(">>> Server ran postProductServiceImpl.getAllPostProduct()");

        Map<String, Object> meta = Map.of(
                "currentPage", postProducts.getNumber(),
                "totalElements", postProducts.getTotalElements(),
                "totalPage", postProducts.getTotalPages()
        );

        PostProductListResponse responseData = postProductListMapper.toDto(postProducts.getContent(), meta);

        RestResponse<PostProductListResponse, Object> response = responseMapper.toDto(
                true,
                "POST PRODUCT LIST",
                responseData,
                null
        );

        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }

//    @PreAuthorize("hasRole('ROLE_SELLER')")
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

//    @PreAuthorize("hasRole('ROLE_SELLER')")
    @Operation(summary = "Decide Post Product Verified API",
            description = "Return a result show that post product decision")
    @PostMapping("/review-post-product-decision")
    public ResponseEntity<RestResponse<PostProductResponse, Object>> reviewPostProductDecision(@Valid @RequestBody PostProductDecisionRequest request) throws Exception {
        PostProduct result = postProductServiceImpl.checkPostProductVerification(request);
        PostProductResponse responseData = postProductMapper.toDto(result);
        RestResponse response = responseMapper.toDto(
                true,
                "POST HAS BEEN CHECKED",
                responseData,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }


}
