package Green_trade.green_trade_platform.task;

import Green_trade.green_trade_platform.enumerate.OrderStatus;
import Green_trade.green_trade_platform.enumerate.TransactionStatus;
import Green_trade.green_trade_platform.model.Order;
import Green_trade.green_trade_platform.model.Transaction;
import Green_trade.green_trade_platform.repository.OrderRepository;
import Green_trade.green_trade_platform.service.SystemConfigService;
import Green_trade.green_trade_platform.service.implement.OrderServiceImpl;
import Green_trade.green_trade_platform.service.implement.SystemWalletServiceImpl;
import Green_trade.green_trade_platform.service.implement.TransactionServiceImpl;
import Green_trade.green_trade_platform.util.DateUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class OrderAutoCompleteTask {
    private final OrderRepository orderRepository;
    private final SystemConfigService systemConfigService;
    private final OrderServiceImpl orderService;
    private final SystemWalletServiceImpl systemWalletService;
    private final TransactionServiceImpl transactionService;

    @Scheduled(cron = "0 */2 * * * ?") // Run every 2 minutes
    @Transactional
    public void autoCompleteDeliveredOrders() {
        try {
            log.info(">>> [OrderAutoCompleteTask] Starting auto complete delivered orders task");

            LocalDateTime currentTime = DateUtils.getCurrentVietnamTime();
            log.info(">>> [OrderAutoCompleteTask] Current time: {}", currentTime);

            // Lấy config thời gian từ system config
            long completedSeconds = systemConfigService.getOrderDeliveredToCompletedSeconds();
            long completedDays = completedSeconds / 86400;
            log.info(">>> [OrderAutoCompleteTask] Config: {} seconds ({} days)", completedSeconds, completedDays);

            // Tìm tất cả orders có status DELIVERED
            List<Order> deliveredOrders = orderRepository.findAllByStatus(OrderStatus.DELIVERED);
            log.info(">>> [OrderAutoCompleteTask] Found {} orders with DELIVERED status", deliveredOrders.size());

            // Lọc các orders đã đủ thời gian (updatedAt + config seconds <= currentTime)
            List<Order> ordersToComplete = deliveredOrders.stream()
                    .filter(order -> {
                        LocalDateTime updatedAt = order.getUpdatedAt();
                        if (updatedAt == null) {
                            // Nếu không có updatedAt, sử dụng createdAt
                            updatedAt = order.getCreatedAt();
                        }
                        LocalDateTime completionTime = updatedAt.plusSeconds(completedSeconds);
                        return completionTime.isBefore(currentTime) || completionTime.isEqual(currentTime);
                    })
                    .toList();

            log.info(">>> [OrderAutoCompleteTask] Found {} orders to auto complete (updatedAt + {} seconds <= currentTime)",
                    ordersToComplete.size(), completedSeconds);

            int successCount = 0;
            int errorCount = 0;

            for (Order order : ordersToComplete) {
                try {
                    log.info(">>> [OrderAutoCompleteTask] Processing order ID: {}, updatedAt: {}",
                            order.getId(), order.getUpdatedAt());

                    // Chuyển status sang COMPLETED
                    order = orderService.updateOrderStatus(order, OrderStatus.COMPLETED);
                    log.info(">>> [OrderAutoCompleteTask] Updated order {} status to COMPLETED", order.getId());

                    // Cập nhật system wallet endAt nếu có
                    if (order.getSystemWallet() != null) {
                        systemWalletService.updateTimeWhenBuyerReceivedProduct(order.getSystemWallet());
                        log.info(">>> [OrderAutoCompleteTask] Updated system wallet endAt for order {}", order.getId());
                    }

                    // Xử lý transaction cho COD nếu chưa có transaction SUCCESS
                    if (order.getTransactions() != null && !order.getTransactions().isEmpty()) {
                        String paymentGateway = order.getTransactions().getLast().getPayment().getGatewayName();

                        // Kiểm tra xem transaction SUCCESS đã có chưa
                        boolean hasSuccessTransaction = order.getTransactions().stream()
                                .anyMatch(t -> t.getStatus().equals(TransactionStatus.SUCCESS));

                        if ("COD".equalsIgnoreCase(paymentGateway) && !hasSuccessTransaction) {
                            Transaction transaction = transactionService.createTransaction(
                                    order,
                                    TransactionStatus.SUCCESS,
                                    order.getTransactions().getLast().getPayment()
                            );
                            log.info(">>> [OrderAutoCompleteTask] Created transaction for COD order {}", order.getId());
                        }
                    }

                    successCount++;
                    log.info(">>> [OrderAutoCompleteTask] Successfully auto completed order ID: {}", order.getId());

                } catch (Exception e) {
                    errorCount++;
                    log.error(">>> [OrderAutoCompleteTask] Error auto completing order ID: {}",
                            order.getId(), e);
                    // Tiếp tục xử lý các order khác nếu một order bị lỗi
                }
            }

            log.info(">>> [OrderAutoCompleteTask] Completed auto complete delivered orders task. " +
                            "Processed: {}, Success: {}, Errors: {}",
                    ordersToComplete.size(), successCount, errorCount);

        } catch (Exception e) {
            log.error(">>> [OrderAutoCompleteTask] Error in auto complete delivered orders task", e);
        }
    }
}

