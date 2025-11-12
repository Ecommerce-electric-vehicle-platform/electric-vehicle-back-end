package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.enumerate.OrderStatus;
import Green_trade.green_trade_platform.enumerate.TransactionStatus;
import Green_trade.green_trade_platform.exception.OrderNotFound;
import Green_trade.green_trade_platform.model.*;
import Green_trade.green_trade_platform.repository.CancelOrderReasonRepository;
import Green_trade.green_trade_platform.repository.OrderRepository;
import Green_trade.green_trade_platform.repository.PostProductRepository;
import Green_trade.green_trade_platform.repository.TransactionRepository;
import Green_trade.green_trade_platform.request.CancelOrderRequest;
import Green_trade.green_trade_platform.service.OrderService;
import Green_trade.green_trade_platform.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import Green_trade.green_trade_platform.util.DateUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final TransactionServiceImpl transactionService;
    private final GhnServiceImpl ghnServiceImpl;
    private final WalletTransactionServiceImpl walletTransactionServiceImpl;
    private final WalletServiceImpl walletService;
    private final TransactionRepository transactionRepository;
    private final CancelOrderReasonRepository cancelOrderReasonRepository;
    private final PostProductRepository postProductRepository;

    public Page<Order> getOrdersOfCurrentUserPaging(int size, int page, Buyer buyer) {
        try {
            log.info(">>> [OrderServiceImpl] camed getOrdersOfCurrentUserPaging");
            Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
            log.info(">>> [OrderServiceImpl] created pageable successfully");
            Page<Order> ordersPage = orderRepository.findAllByBuyer(buyer, pageable);
            log.info(">>> [OrderServiceImpl] created orderPage successfully");
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
        log.info(">>> [OrderServiceImpl] updateOrderCode - orderCode: {}, orderId: {}", orderCode, order.getId());
        order.setOrderCode(orderCode);
        Order result = orderRepository.save(order);
        log.info(">>> [OrderServiceImpl] updateOrderCode - result: {}", result);
        return result;
    }

    public Order updateSystemWallet(SystemWallet systemWallet, Order order) {
        log.info(">>> [OrderServiceImpl] updateSystemWallet - systemWalletId: {}, orderId: {}", systemWallet.getId(), order.getId());
        order.setSystemWallet(systemWallet);
        Order result = orderRepository.save(order);
        log.info(">>> [OrderServiceImpl] updateSystemWallet - result: {}", result);
        return result;
    }

    public Order updateOrderTransactions(Order order, List<Transaction> transactions) {
        log.info(">>> [OrderServiceImpl] updateOrderTransactions - orderId: {}, transactionsCount: {}", order.getId(), transactions.size());
        order.setTransactions(transactions);
        Order result = orderRepository.save(order);
        log.info(">>> [OrderServiceImpl] updateOrderTransactions - result: {}", result);
        return result;
    }

    public Order updateOrderStatus(Order order, OrderStatus status) {
        log.info(">>> [OrderServiceImpl] updateOrderStatus - orderId: {}, newStatus: {}", order.getId(), status);
        order.setStatus(status);
        Order result = orderRepository.save(order);
        log.info(">>> [OrderServiceImpl] updateOrderStatus - result: {}", result);
        return result;
    }

    public Order confirmOrder(Long orderId, Buyer buyer) throws Exception {
        log.info(">>> [OrderServiceImpl] confirmOrder - orderId: {}, buyerId: {}", orderId, buyer.getBuyerId());
        
        // Tìm order
        Order order = orderRepository.findOrderById(orderId)
                .orElseThrow(() -> new OrderNotFound());
        
        log.info(">>> [OrderServiceImpl] Found order: orderId={}, buyerId={}, status={}", 
                order.getId(), order.getBuyer().getBuyerId(), order.getStatus());
        
        // Kiểm tra order thuộc về buyer
        if (!order.getBuyer().getBuyerId().equals(buyer.getBuyerId())) {
            throw new Exception("Order does not belong to this buyer");
        }
        
        // Kiểm tra order status phải là DELIVERED
        if (order.getStatus().equals(OrderStatus.COMPLETED)) {
            log.info(">>> [OrderServiceImpl] Order already completed");
            return order;
        }
        
        if (!order.getStatus().equals(OrderStatus.DELIVERED)) {
            throw new Exception("Order status must be DELIVERED to confirm. Current status: " + order.getStatus());
        }
        
        // Chuyển status sang COMPLETED
        order = updateOrderStatus(order, OrderStatus.COMPLETED);
        log.info(">>> [OrderServiceImpl] Updated order status to COMPLETED");
        
        return order;
    }

    public Order cancelOrder(Long id, CancelOrderRequest request) throws Exception {
        try {
            log.info(">>> [OrderServiceImpl] came cancelOrder");
            log.info(">>> request: {}", request);
            Optional<Order> orderOpt = orderRepository.findOrderById((id));
            if (orderOpt.isEmpty()) {
                throw new OrderNotFound();
            }
            log.info(">>> [OrderServiceImpl] found order successfully");

            CancelOrderReason cancelOrderReason = cancelOrderReasonRepository.findById(request.getCancelReasonId())
                    .orElseThrow(
                            () -> new Exception("Cancel Order Reason Not found")
                    );

            Order orderFound = orderOpt.get();

            log.info(">>> [OrderServiceImpl] orderFound: {}", orderFound);
            if (orderFound.getStatus().equals(OrderStatus.PENDING)) {
                log.info(">>> [OrderServiceImpl] order pending status");
                Transaction transaction = transactionService.createTransaction(orderFound, TransactionStatus.CANCELED,
                        orderFound.getTransactions().getLast().getPayment()); //transaction không có thì sẽ lỗi, nên lưu ý
                log.info(">>> [OrderServiceImpl] created transaction successfully");
                orderFound = updateOrderStatus(orderFound, OrderStatus.CANCELED);
                log.info(">>> [OrderServiceImpl] update order status to canceled successfully");
                orderFound.getPostProduct().setSold(false);
                log.info(">>> [OrderServiceImpl] update order sold successfully");
                orderFound.setCancelOrderReason(cancelOrderReason);
                log.info(">>> [OrderServiceImpl] update cancel order reason successfully");
                orderFound.setCanceledAt(DateUtils.getCurrentVietnamTime());
                log.info(">>> [OrderServiceImpl] update canceled at successfully");
            } else if (orderFound.getStatus().equals(OrderStatus.PAID)) {
                log.info(">>> [OrderServiceImpl] order paid status");
                Transaction transaction = transactionService.createTransaction(orderFound, TransactionStatus.CANCELED,
                        orderFound.getTransactions().getLast().getPayment());
                log.info(">>> [OrderServiceImpl] created transaction successfully");
                orderFound = updateOrderStatus(orderFound, OrderStatus.CANCELED);
                log.info(">>> [OrderServiceImpl] update order status to canceled successfully");
                walletService.handleBuyerRefundForCancelledOrder(orderFound.getSystemWallet(), 100, orderFound.getBuyer().getWallet());
                log.info(">>> [OrderServiceImpl] refund successfully");
                orderFound.getPostProduct().setSold(false);
                log.info(">>> [OrderServiceImpl] update order sold successfully");
                orderFound.setCancelOrderReason(cancelOrderReason);
                log.info(">>> [OrderServiceImpl] update cancel order reason successfully");
                orderFound.setCanceledAt(DateUtils.getCurrentVietnamTime());
                log.info(">>> [OrderServiceImpl] update canceled at successfully");
            } else {
                throw new Exception("Cannot cancel order");
            }
            return orderRepository.save(orderFound);
        } catch (Exception e) {
            log.info(">>> [OrderServiceImpl] Error at cancelOrder: {}", e.getMessage());
            throw e;
        }
    }

    public Page<Order> getPendingOrders(Seller seller, int page, int size) {
        log.info(">>> [OrderServiceImpl] getPendingOrders - sellerId: {}, page: {}, size: {}", seller.getSellerId(), page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> result = orderRepository.findByPostProduct_SellerAndStatus(seller, OrderStatus.PENDING, pageable);
        log.info(">>> [OrderServiceImpl] getPendingOrders - result: {} orders found", result.getTotalElements());
        return result;
    }

    public Order verifyOrder(long id) {
        log.info(">>> [OrderServiceImpl] verifyOrder - orderId: {}", id);
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Can not find order with this order id: " + id)
        );
        log.info(">>> [OrderServiceImpl] verifyOrder - found order: {}", order);

        order.setStatus(OrderStatus.VERIFIED);
        Order result = orderRepository.save(order);
        log.info(">>> [OrderServiceImpl] verifyOrder - result: {}", result);
        return result;
    }

    public Order getOrderById(Long orderId) {
        log.info(">>> [OrderServiceImpl] getOrderById - orderId: {}", orderId);
        Order result = null;
        Optional<Order> orderOpt = orderRepository.findOrderById(orderId);
        if (orderOpt.isPresent()) {
            result = orderOpt.get();
            log.info(">>> [OrderServiceImpl] getOrderById - found order: {}", result);
        } else {
            log.info(">>> [OrderServiceImpl] getOrderById - order not found");
        }
        return result;
    }

    public Transaction getTransactionByOrderId(long id) {
        log.info(">>> [Order Service] Get transaction by order id: Started.");
        Order order = getOrderById(id);
        log.info(">>> [Order Service] Order info: {}", order);

        Transaction transaction = transactionRepository.findValidTransactionsByOrderId(order, TransactionStatus.FAIL).orElseThrow(
                () -> new EntityNotFoundException("Can not find transaction with this order id: " + id)
        );
        log.info(">>> [Order Service] Transaction info: {}", transaction);
        return transaction;
    }

    public Page<Order> getAllOrders(int page, int size, Seller seller) {
        log.info(">>> [OrderServiceImpl] getAllOrders - sellerId: {}, page: {}, size: {}", seller.getSellerId(), page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> result = orderRepository.findAllByPostProduct_Seller(seller, pageable);
        log.info(">>> [OrderServiceImpl] getAllOrders - result: {} orders found", result.getTotalElements());
        return result;
    }

    public Order updateShippingFee(Order order, String shippingFee) {
        log.info(">>> [OrderServiceImpl] updateShippingFee - orderId: {}, shippingFee: {}", order.getId(), shippingFee);
        order.setShippingFee(new BigDecimal(shippingFee));
        Order result = orderRepository.save(order);
        log.info(">>> [OrderServiceImpl] updateShippingFee - result: {}", result);
        return result;
    }

    public int countPendingOrder(Seller seller) {
        log.info(">>> [OrderServiceImpl] countPendingOrder - sellerId: {}", seller.getSellerId());
        int result = orderRepository.countByStatusAndSeller(OrderStatus.PENDING, seller.getSellerId());
        log.info(">>> [OrderServiceImpl] countPendingOrder - result: {} pending orders", result);
        return result;
    }

    public int countAllOrder(Seller seller) {
        log.info(">>> [OrderServiceImpl] countAllOrder - sellerId: {}", seller.getSellerId());
        int result = orderRepository.countBySeller(seller.getSellerId());
        log.info(">>> [OrderServiceImpl] countAllOrder - result: {} total orders", result);
        return result;
    }

    public BigDecimal getTotalRevenue(Seller seller) {
        log.info(">>> [OrderServiceImpl] getTotalRevenue - sellerId: {}", seller.getSellerId());
        BigDecimal result = orderRepository.getTotalRevenueBySeller(seller.getSellerId());
        log.info(">>> [OrderServiceImpl] getTotalRevenue - result: {}", result);
        return result;
    }
}
