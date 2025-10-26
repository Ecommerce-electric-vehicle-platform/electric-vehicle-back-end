package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.mapper.SubscriptionMapper;
import Green_trade.green_trade_platform.model.Seller;
import Green_trade.green_trade_platform.model.Subscription;
import Green_trade.green_trade_platform.request.SignPackageRequest;
import Green_trade.green_trade_platform.response.SubscriptionPackageResponse;
import Green_trade.green_trade_platform.service.SellerService;
import Green_trade.green_trade_platform.service.implement.SellerServiceImpl;
import Green_trade.green_trade_platform.service.implement.SubscriptionPackageServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/packages")
@RequiredArgsConstructor
public class SubscriptionPackageController {

    private final SubscriptionPackageServiceImpl subscriptionPackageService;
    private final ResponseMapper responseMapper;
    private final SellerServiceImpl sellerService;
    private final SubscriptionMapper subscriptionMapper;

    @Operation(
            summary = "Trả về các gói (active) của hệ thống dành cho seller,",
            description = "Trả về các gói của hệ thống để người dùng đăng ký."
    )
    @GetMapping("/active")
    public ResponseEntity<?> getActivePackages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(responseMapper.toDto(true,
                "Đã lấy thành công các gói.",
                subscriptionPackageService.getActivePackageResponses(PageRequest.of(page, size)), null));
    }

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @PostMapping("/sign-package")
    public ResponseEntity<?> signPackage(@RequestBody SignPackageRequest request) {
        Map<String, Object> ans = subscriptionPackageService.handlesignPackage(request);
        if(true == (Boolean) ans.get("success")) {
            return ResponseEntity.ok(responseMapper.toDto(true,
                    "Đăng kí gói người bán thành công.",
                    ans, null));
        } else {
            return ResponseEntity.badRequest().body(responseMapper.toDto(false,
                    "Số dư ví không đủ, vui lòng nạp thêm.",
                    null, ans));
        }
    }

    @PreAuthorize("hasRole('ROLE_SELLER')")
    @Operation(
            summary = "Cancel current seller subscription package",
            description = """
        Allows an authenticated seller to cancel their active subscription package.  
        Once this endpoint is called, the seller's current package will be marked as canceled, 
        and no further benefits or billing will be applied after the current billing cycle.  
        
        **Access control:** Only users with the `ROLE_SELLER` authority can call this API.  
        
        **Example use case:**  
        A seller wants to stop their current premium plan and revert to a basic account.  
        They trigger this endpoint to mark the package as canceled.
        """
    )
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelSubscription() {
        try {
            Seller seller = sellerService.getCurrentUser();
            subscriptionPackageService.cancelSubscription(seller);
            return  ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "CANCEL SUBSCRIPTION PACKAGE SUCCESSFULLY.",
                    null, null
            ));
        } catch (Exception e) {
            return  ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "CANCEL SUBSCRIPTION PACKAGE FAILED.",
                    null, e.getMessage()
            ));
        }
    }

    @Operation(
            summary = "Get current active subscription package",
            description = """
        Retrieve the seller's currently active subscription package.  
        This endpoint returns detailed information about the seller's current plan, including 
        its type, status, start date, end date, and any remaining duration or benefits.  

        **Access control:**  
        - Only authenticated users with the role `ROLE_SELLER` can call this endpoint.  

        **Example use case:**  
        A seller opens their account dashboard and wants to view details of their current 
        subscription plan — for example, to see when it expires or whether auto-renewal is active.
        """
    )
    @PreAuthorize("hasRole('ROLE_SELLER')")
    @GetMapping("current-subscription")
    public ResponseEntity<?> getCurrentSubscription() {
        log.info(">>> [Subscription controller] Starting");
        try {
            Seller seller = sellerService.getCurrentUser();
            Subscription subscription = subscriptionPackageService.getCurrentSubscription(seller);
            Map<String, Object> data = new HashMap<>();
            String username = seller.getSellerName();

            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET CURRENT SUBSCRIPTION SUCCESSFULLY.",
                    subscriptionMapper.toDto(subscription), null
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET CURRENT SUBSCRIPTION FAILED.",
                    null, e.getMessage()
            ));
        }
    }


}