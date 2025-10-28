package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.model.DisputeCategory;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.service.implement.DisputeCategoryServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dispute-category")
public class DisputeCategoryController {

    private final DisputeCategoryServiceImpl disputeCategoryService;
    private final ResponseMapper responseMapper;

    public DisputeCategoryController(DisputeCategoryServiceImpl disputeCategoryService, ResponseMapper responseMapper) {
        this.disputeCategoryService = disputeCategoryService;
        this.responseMapper = responseMapper;
    }

    @GetMapping("/dispute-categories")
    public ResponseEntity<RestResponse<?, ?>> getAllDisputeCategory() {
        List<DisputeCategory> disputeCategories = disputeCategoryService.getAllDisputeCategory();
        RestResponse<?, ?> response = responseMapper.toDto(
                true,
                "FETCH DISPUTE CATEGORY LIST",
                null,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }
}
