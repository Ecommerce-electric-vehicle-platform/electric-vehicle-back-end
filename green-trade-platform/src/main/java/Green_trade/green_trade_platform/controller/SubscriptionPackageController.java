package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.response.SubscriptionPackageResponse;
import Green_trade.green_trade_platform.service.implement.SubscriptionPackageServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/packages")
@RequiredArgsConstructor
public class SubscriptionPackageController {

    private final SubscriptionPackageServiceImpl subscriptionPackageService;
    private final ResponseMapper responseMapper;

    @GetMapping("/active")
    public ResponseEntity<?> getActivePackages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(responseMapper.toDto(true,
                "Đã lấy thành công các gói.",
                subscriptionPackageService.getActivePackageResponses(PageRequest.of(page, size)), null));
    }


}