package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.request.SignPackageRequest;
import Green_trade.green_trade_platform.response.SubscriptionPackageResponse;
import Green_trade.green_trade_platform.service.implement.SubscriptionPackageServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/packages")
@RequiredArgsConstructor
public class SubscriptionPackageController {

    private final SubscriptionPackageServiceImpl subscriptionPackageService;
    private final ResponseMapper responseMapper;

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
}