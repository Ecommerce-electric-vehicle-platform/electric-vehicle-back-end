package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.DisputeCategoryMapper;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.model.DisputeCategory;
import Green_trade.green_trade_platform.response.DisputeCategoryResponse;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.service.implement.DisputeCategoryServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dispute-category")
public class DisputeCategoryController {

    private final DisputeCategoryServiceImpl disputeCategoryService;
    private final ResponseMapper responseMapper;
    private final DisputeCategoryMapper disputeCategoryMapper;

    public DisputeCategoryController(DisputeCategoryServiceImpl disputeCategoryService, ResponseMapper responseMapper, DisputeCategoryMapper disputeCategoryMapper) {
        this.disputeCategoryService = disputeCategoryService;
        this.responseMapper = responseMapper;
        this.disputeCategoryMapper = disputeCategoryMapper;
    }

    @GetMapping("/dispute-categories")
    public ResponseEntity<RestResponse<List<DisputeCategoryResponse>, Object>> getAllDisputeCategory() {
        List<DisputeCategoryResponse> responseData = new ArrayList<>();
        List<DisputeCategory> disputeCategories = disputeCategoryService.getAllDisputeCategory();
        disputeCategories.forEach(disputeCategory -> responseData.add(disputeCategoryMapper.toDto(disputeCategory)));
        RestResponse<List<DisputeCategoryResponse>, Object> response = responseMapper.toDto(
                true,
                "FETCH DISPUTE CATEGORY LIST",
                responseData,
                null
        );
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }
}
