package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.enumerate.OrderStatus;
import Green_trade.green_trade_platform.enumerate.TransactionStatus;
import Green_trade.green_trade_platform.exception.OrderNotFound;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.Order;
import Green_trade.green_trade_platform.model.Transaction;
import Green_trade.green_trade_platform.repository.OrderRepository;
import Green_trade.green_trade_platform.service.OrderService;
import Green_trade.green_trade_platform.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final TransactionServiceImpl transactionService;
    private final GhnServiceImpl ghnServiceImpl;
    private final WalletTransactionServiceImpl walletTransactionServiceImpl;
    private final WalletServiceImpl walletService;

    public OrderServiceImpl(OrderRepository orderRepository, TransactionServiceImpl transactionService,
            GhnServiceImpl ghnServiceImpl, WalletTransactionServiceImpl walletTransactionServiceImpl,
            WalletServiceImpl walletService) {
        this.orderRepository = orderRepository;
        this.transactionService = transactionService;
        this.ghnServiceImpl = ghnServiceImpl;
        this.walletTransactionServiceImpl = walletTransactionServiceImpl;
        this.walletService = walletService;
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

    public Order cancelOrder(Long id) throws Exception {
        try {
            Optional<Order> orderOpt = orderRepository.findOrderById((id));
            if (orderOpt.isEmpty()) {
                throw new OrderNotFound();
            }

            Order orderFound = orderOpt.get();

            if (orderFound.getStatus().equals(OrderStatus.PENDING)) {
                Transaction transaction = transactionService.createTransaction(orderFound, TransactionStatus.CANCELED,
                        orderFound.getTransactions().getLast().getPayment());
                orderFound = updateOrderStatus(orderFound, OrderStatus.CANCELED);
            } else if (orderFound.getStatus().equals(OrderStatus.PAID)) {
                Transaction transaction = transactionService.createTransaction(orderFound, TransactionStatus.CANCELED,
                        orderFound.getTransactions().getLast().getPayment());
                orderFound = updateOrderStatus(orderFound, OrderStatus.CANCELED);
                walletTransactionServiceImpl.handleRefundMoney(orderFound.getBuyer().getWallet(), orderFound.getPrice(),
                        true, "REFUNDED FROM CANCELED ORDER");
                walletService.handleBuyerRefund(orderFound.getSystemWallet(), 100, orderFound.getBuyer().getWallet(),
                        false);
            } else {
                throw new Exception("Cannot cancel order");
            }
            return orderFound;
        } catch (Exception e) {
            log.info(">>> [OrderServiceImpl] Error at cancelOrder: {}", e.getMessage());
            throw e;
        }
    }
}
