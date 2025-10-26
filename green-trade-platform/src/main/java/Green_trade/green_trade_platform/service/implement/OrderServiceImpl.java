package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.enumerate.OrderStatus;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.Order;
import Green_trade.green_trade_platform.model.Transaction;
import Green_trade.green_trade_platform.repository.OrderRepository;
import Green_trade.green_trade_platform.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Map<String, Object> saveOrder(Order newOrder) {
        log.info(">>> start save order service");
        Map<String, Object> data = new HashMap<>();
        try {
            Order order = orderRepository.save(newOrder);
            data.put("success", true);
            data.put("message", "save order successfully.");
            data.put("data", order);
        } catch (Exception e) {
            data.put("success", false);
            data.put("message", e.getMessage());
        }
        log.info(">>> save order service: {}", data.toString());
        return data;
    }

    public Order updateOrderCode(String orderCode, Order order) {
        order.setOrderCode(orderCode);
        return orderRepository.save(order);
    }

    public Order updateOrderTransactions(Order order, List<Transaction> transactions) {
        order.setTransactions(transactions);
        return orderRepository.save(order);
    }

    public Order updateOrderStatus(Order order, OrderStatus status) {
        order.setStatus(status);
        return orderRepository.save(order);
    }
}
