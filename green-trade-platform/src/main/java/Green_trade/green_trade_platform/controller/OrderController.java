package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.OrderListMapper;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.Order;
import Green_trade.green_trade_platform.response.OrderListResponse;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.service.implement.BuyerServiceImpl;
import Green_trade.green_trade_platform.service.implement.OrderServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/order")
@Slf4j
public class OrderController {

    private final OrderServiceImpl orderService;
    private final ResponseMapper responseMapper;
    private final OrderListMapper orderListMapper;
    private final BuyerServiceImpl buyerService;

    public OrderController(
            OrderServiceImpl orderService,
            ResponseMapper responseMapper,
            OrderListMapper orderListMapper, BuyerServiceImpl buyerService) {
        this.orderService = orderService;
        this.responseMapper = responseMapper;
        this.orderListMapper = orderListMapper;
        this.buyerService = buyerService;
    }

    @Operation(
            summary = "API for request order history of user",
            description = "Return a paging list of order"
    )
    @GetMapping("/history")
    public ResponseEntity<RestResponse<OrderListResponse, Object>> getOrdersHistoryOfCurrentUser(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        try {
            Buyer buyer = buyerService.getCurrentUser();
            Page<Order> orderPaging = orderService.getOrdersOfCurrentUserPaging(size, page, buyer);

            Map<String, Object> meta = Map.of(
                    "currentPage", orderPaging.getNumber(),
                    "totalElements", orderPaging.getTotalElements(),
                    "totalPage", orderPaging.getTotalPages()
            );

            OrderListResponse orderListResponse = orderListMapper.toDto(orderPaging.toList(), meta);

            RestResponse<OrderListResponse, Object> response = responseMapper.toDto(
                    true,
                    "FETCH ORDER HISTORY SUCCESSFULLY",
                    orderListResponse,
                    null
            );

            return ResponseEntity.status(HttpStatus.OK.value()).body(response);
        } catch (Exception e) {
            log.info(">>> Error at getOrdersHistoryOfCurrentUser: {}", e.getMessage());
            throw e;
        }
    }
}
