package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.Order;
import Green_trade.green_trade_platform.repository.OrderRepository;
import Green_trade.green_trade_platform.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Page<Order> getOrdersOfCurrentUserPaging(int size, int page, Buyer buyer) {
        try {
            Pageable pageable = PageRequest.of(size, page, Sort.by("order_id"));
            Page<Order> ordersPage = orderRepository.findAllByBuyer(buyer, pageable);
            return new PageImpl<>(ordersPage.getContent(), pageable, ordersPage.getTotalElements());
        } catch (Exception e) {
            log.info(">>> Error at getOrdersOfCurrentUserPaging: {}", e.getMessage());
            throw e;
        }
    }
}
