package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.enumerate.SystemWalletStatus;
import Green_trade.green_trade_platform.exception.SystemWalletException;
import Green_trade.green_trade_platform.model.Order;
import Green_trade.green_trade_platform.model.SystemConfig;
import Green_trade.green_trade_platform.model.SystemWallet;
import Green_trade.green_trade_platform.repository.SystemConfigRepository;
import Green_trade.green_trade_platform.repository.SystemWalletRepossitory;
import Green_trade.green_trade_platform.request.RefundResolveRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import Green_trade.green_trade_platform.util.DateUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class SystemWalletServiceImpl {
    private final SystemWalletRepossitory systemWalletRepossitory;
    private final SystemConfigRepository systemConfigRepository;
    private static final String ESCROW_TRANSFER_SECONDS_KEY = "ESCROW_TRANSFER_SECONDS";
    // Default: 14 ngày = 14 * 24 * 60 * 60 = 1,209,600 giây
    private static final long DEFAULT_ESCROW_TRANSFER_SECONDS = 1209600L;

    public long getEscrowTransferSeconds() {
        try {
            SystemConfig config = systemConfigRepository.findByConfigKey(ESCROW_TRANSFER_SECONDS_KEY)
                    .orElse(null);
            if (config != null) {
                return Long.parseLong(config.getConfigValue());
            }
        } catch (NumberFormatException e) {
            log.warn(">>> [SystemConfigService] Invalid escrow transfer seconds config value, using default: {} seconds ({} days)",
                    DEFAULT_ESCROW_TRANSFER_SECONDS, DEFAULT_ESCROW_TRANSFER_SECONDS / 86400);
        }
        return DEFAULT_ESCROW_TRANSFER_SECONDS;
    }

    public void handleRefund(SystemWallet systemWallet) {
        systemWallet.setStatus(SystemWalletStatus.IS_SOLVED);
        systemWallet.setEndAt(DateUtils.getCurrentVietnamTime());
        systemWalletRepossitory.save(systemWallet);
    }

    public SystemWallet getSystemWalletByOrder(Order order) {
        return systemWalletRepossitory.findByOrder(order).orElseThrow(
                () -> new IllegalArgumentException("COD order cannot apply refund money from system wallet.")
        );
    }

    public SystemWallet createEscrowRecord(Order order) {
        try {
            log.info(">>> [SystemWalletServiceImpl] the system came createEscrowRecord");
            SystemWallet escrowRecord = SystemWallet.builder()
                    .admin(null)
                    .order(order)
                    .buyerWalletId(order.getBuyer().getWallet().getWalletId())
                    .sellerWalletId(order.getPostProduct().getSeller().getBuyer().getWallet().getWalletId())
                    .concurrency("VND")
                    .balance(order.getPrice())
                    .status(SystemWalletStatus.ESCROW_HOLD)
                    .endAt(null) // endAt sẽ được cập nhật khi order được complete
                    .build();
            log.info(">>> [SystemWalletServiceImpl] create new escrowRecord");
            return systemWalletRepossitory.save(escrowRecord);
        } catch (Exception e) {
            log.info(">>> [SystemWalletServiceImpl] Error at createEscrowRecord: {}", e.getMessage());
            throw new SystemWalletException();
        }
    }

    public SystemWallet createEscrowRecordAfterReduceFeeCOD(Order order, String totalFee) {
        try {
            log.info(">>> [SystemWalletServiceImpl] the system came createEscrowRecordAfterReduceFeeCOD");
            BigDecimal productPrice = order.getPrice();
            log.info(">>> [SystemWalletServiceImpl] productPrice: {}", productPrice);
            BigDecimal shippingFee = order.getShippingFee();
            log.info(">>> [SystemWalletServiceImpl] shippingFee: {}", shippingFee);
            BigDecimal totalFeeInNumber = new BigDecimal(totalFee);
            log.info(">>> [SystemWalletServiceImpl] totalFeeInNumber: {}", totalFeeInNumber);
            BigDecimal actualReceivedMoney = productPrice;
            log.info(">>> [SystemWalletServiceImpl] actualReceivedMoney: {}", actualReceivedMoney);
            SystemWallet escrowRecord = SystemWallet.builder()
                    .admin(null)
                    .order(order)
                    .buyerWalletId(order.getBuyer().getWallet().getWalletId())
                    .sellerWalletId(order.getPostProduct().getSeller().getBuyer().getWallet().getWalletId())
                    .concurrency("VND")
                    .balance(actualReceivedMoney)
                    .status(SystemWalletStatus.ESCROW_HOLD)
                    .endAt(null) // endAt sẽ được cập nhật khi order được complete
                    .build();
            log.info(">>> [SystemWalletServiceImpl] create new escrowRecord");
            return systemWalletRepossitory.save(escrowRecord);
        } catch (Exception e) {
            log.info(">>> [SystemWalletServiceImpl] Error at createEscrowRecord: {}", e.getMessage());
            throw new SystemWalletException();
        }
    }

    public SystemWallet createEscrowRecordForCOD(Order order, String totalFee) {
        try {
            log.info(">>> [SystemWalletServiceImpl] the system came createEscrowRecordAfterReduceFeeCOD");
            BigDecimal productPrice = order.getPrice();
            log.info(">>> [SystemWalletServiceImpl] productPrice: {}", productPrice);
            BigDecimal shippingFee = order.getShippingFee();
            log.info(">>> [SystemWalletServiceImpl] shippingFee: {}", shippingFee);
            BigDecimal totalFeeInNumber = new BigDecimal(totalFee);
            log.info(">>> [SystemWalletServiceImpl] totalFeeInNumber: {}", totalFeeInNumber);
            BigDecimal actualReceivedMoney = productPrice;
            log.info(">>> [SystemWalletServiceImpl] actualReceivedMoney: {}", actualReceivedMoney);
            SystemWallet escrowRecord = SystemWallet.builder()
                    .admin(null)
                    .order(order)
                    .buyerWalletId(order.getBuyer().getWallet().getWalletId())
                    .sellerWalletId(order.getPostProduct().getSeller().getBuyer().getWallet().getWalletId())
                    .concurrency("VND")
                    .balance(actualReceivedMoney)
                    .shippingFee(order.getShippingFee())
                    .status(SystemWalletStatus.ESCROW_HOLD)
                    .endAt(null)
                    .build();
            log.info(">>> [SystemWalletServiceImpl] create new escrowRecord");
            return systemWalletRepossitory.save(escrowRecord);
        } catch (Exception e) {
            log.info(">>> [SystemWalletServiceImpl] Error at createEscrowRecord: {}", e.getMessage());
            throw new SystemWalletException();
        }
    }

    public SystemWallet createEscrowRecordAfterReduceFeeCOD(Order order, BigDecimal totalFee) {
        try {
            log.info(">>> [SystemWalletServiceImpl] the system came createEscrowRecordAfterReduceFeeCOD");
            BigDecimal productPrice = order.getPrice();
            log.info(">>> [SystemWalletServiceImpl] productPrice: {}", productPrice);
            BigDecimal shippingFee = order.getShippingFee();
            log.info(">>> [SystemWalletServiceImpl] shippingFee: {}", shippingFee);
            BigDecimal totalFeeInNumber = totalFee;
            log.info(">>> [SystemWalletServiceImpl] totalFeeInNumber: {}", totalFeeInNumber);
            BigDecimal actualReceivedMoney = productPrice;
            log.info(">>> [SystemWalletServiceImpl] actualReceivedMoney: {}", actualReceivedMoney);
            SystemWallet escrowRecord = SystemWallet.builder()
                    .admin(null)
                    .order(order)
                    .buyerWalletId(order.getBuyer().getWallet().getWalletId())
                    .sellerWalletId(order.getPostProduct().getSeller().getBuyer().getWallet().getWalletId())
                    .concurrency("VND")
                    .balance(actualReceivedMoney)
                    .status(SystemWalletStatus.ESCROW_HOLD)
                    .endAt(null) // endAt sẽ được cập nhật khi order được complete
                    .build();
            log.info(">>> [SystemWalletServiceImpl] create new escrowRecord");
            return systemWalletRepossitory.save(escrowRecord);
        } catch (Exception e) {
            log.info(">>> [SystemWalletServiceImpl] Error at createEscrowRecord: {}", e.getMessage());
            throw new SystemWalletException();
        }
    }

    public SystemWallet createEscrowRecordAfterReduceFeeWalletPayment(Order order, String totalFee) {
        try {
            log.info(">>> 1 ");
            BigDecimal productPrice = order.getPrice();
            log.info(">>> 1 ");
            BigDecimal shippingFee = order.getShippingFee();
            log.info(">>> 1 ");
            BigDecimal totalFeeInNumber = new BigDecimal(totalFee);
            log.info(">>> 1 ");
            BigDecimal actualReceivedMoney = productPrice;
            log.info(">>> 1 ");
            log.info(">>> [SystemWalletServiceImpl] the system came createEscrowRecordAfterReduceFeeWalletPayment");
            SystemWallet escrowRecord = SystemWallet.builder()
                    .admin(null)
                    .order(order)
                    .buyerWalletId(order.getBuyer().getWallet().getWalletId())
                    .sellerWalletId(order.getPostProduct().getSeller().getBuyer().getWallet().getWalletId())
                    .concurrency("VND")
                    .balance(actualReceivedMoney)
                    .status(SystemWalletStatus.ESCROW_HOLD)
                    .createdAt(null)
                    .endAt(null)
                    .build();
            log.info(">>> [SystemWalletServiceImpl] create new escrowRecord");
            return systemWalletRepossitory.save(escrowRecord);
        } catch (Exception e) {
            log.info(">>> [SystemWalletServiceImpl] Error at createEscrowRecord: {}", e.getMessage());
            throw new SystemWalletException();
        }
    }

    public SystemWallet createEscrowRecordForWalletPayment(Order order, String totalFee) {
        try {
            log.info(">>> 1 ");
            BigDecimal productPrice = order.getPrice();
            log.info(">>> 1 ");
            BigDecimal shippingFee = order.getShippingFee();
            log.info(">>> 1 ");
            BigDecimal totalFeeInNumber = new BigDecimal(totalFee);
            log.info(">>> 1 ");
            BigDecimal actualReceivedMoney = productPrice;
            log.info(">>> 1 ");
            log.info(">>> [SystemWalletServiceImpl] the system came createEscrowRecordAfterReduceFeeWalletPayment");
            SystemWallet escrowRecord = SystemWallet.builder()
                    .admin(null)
                    .order(order)
                    .buyerWalletId(order.getBuyer().getWallet().getWalletId())
                    .sellerWalletId(order.getPostProduct().getSeller().getBuyer().getWallet().getWalletId())
                    .concurrency("VND")
                    .balance(actualReceivedMoney)
                    .shippingFee(order.getShippingFee())
                    .status(SystemWalletStatus.ESCROW_HOLD)
                    .endAt(null) // endAt sẽ được cập nhật khi order được complete
                    .build();
            log.info(">>> [SystemWalletServiceImpl] create new escrowRecord");
            return systemWalletRepossitory.save(escrowRecord);
        } catch (Exception e) {
            log.info(">>> [SystemWalletServiceImpl] Error at createEscrowRecord: {}", e.getMessage());
            throw new SystemWalletException();
        }
    }

    public SystemWallet updateEscrowRecordStatus(SystemWallet escrowRecord, SystemWalletStatus status) {
        escrowRecord.setStatus(status);
        return systemWalletRepossitory.save(escrowRecord);
    }

    /**
     * Cập nhật endAt của system wallet khi đơn hàng được complete
     * Chỉ cập nhật endAt, không thay đổi createdAt
     *
     * @param systemWallet System wallet cần cập nhật
     * @return SystemWallet đã được cập nhật
     */
    public SystemWallet updateTimeWhenBuyerReceivedProduct(SystemWallet systemWallet) {
        if (systemWallet == null) {
            throw new IllegalArgumentException("System wallet cannot be null");
        }

        // Chỉ cập nhật endAt, không thay đổi createdAt
        systemWallet.setEndAt(DateUtils.getCurrentVietnamTime().plusSeconds(getEscrowTransferSeconds()));
        log.info(">>> [SystemWalletServiceImpl] Updated endAt for system wallet ID: {} to {}",
                systemWallet.getId(), systemWallet.getEndAt());
        return systemWalletRepossitory.save(systemWallet);
    }

    public Page<SystemWallet> getAllEscrowService(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return systemWalletRepossitory.findAllByStatus(SystemWalletStatus.ESCROW_HOLD, pageable);
    }

    /**
     * Cập nhật endAt của system wallet (chỉ admin mới được phép)
     *
     * @param systemWalletId ID của system wallet cần cập nhật
     * @param newEndAt       Thời gian endAt mới
     * @return SystemWallet đã được cập nhật
     */
    public SystemWallet updateEndAt(Long systemWalletId, LocalDateTime newEndAt) {
        SystemWallet systemWallet = systemWalletRepossitory.findById(systemWalletId)
                .orElseThrow(() -> new IllegalArgumentException("System wallet not found with id: " + systemWalletId));

        // Chỉ cho phép cập nhật nếu status là ESCROW_HOLD
        if (systemWallet.getStatus() != SystemWalletStatus.ESCROW_HOLD) {
            throw new IllegalArgumentException("Can only update endAt for escrow records with status ESCROW_HOLD. Current status: " + systemWallet.getStatus());
        }

        // Validate: endAt phải sau createdAt
        if (systemWallet.getCreatedAt() != null && newEndAt.isBefore(systemWallet.getCreatedAt())) {
            throw new IllegalArgumentException("End date time must be after created date time.");
        }

        systemWallet.setEndAt(newEndAt);
        log.info(">>> [SystemWalletServiceImpl] Updated endAt for system wallet ID: {} to {}", systemWalletId, newEndAt);

        return systemWalletRepossitory.save(systemWallet);
    }
}
