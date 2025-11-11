package Green_trade.green_trade_platform.task;

import Green_trade.green_trade_platform.enumerate.SystemWalletStatus;
import Green_trade.green_trade_platform.enumerate.TransactionStatus;
import Green_trade.green_trade_platform.enumerate.TransactionType;
import Green_trade.green_trade_platform.model.SystemWallet;
import Green_trade.green_trade_platform.model.Wallet;
import Green_trade.green_trade_platform.model.WalletTransaction;
import Green_trade.green_trade_platform.repository.SystemWalletRepossitory;
import Green_trade.green_trade_platform.repository.WalletRepository;
import Green_trade.green_trade_platform.repository.WalletTransactionRepository;
import Green_trade.green_trade_platform.util.DateUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class EscrowAutoResolveTask {
    private final SystemWalletRepossitory systemWalletRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    @Scheduled(cron = "0 */2 * * * ?") // Run every 2 minutes
    @Transactional
    public void autoResolveEscrow() {
        try {
            log.info(">>> [EscrowAutoResolveTask] Starting auto resolve escrow task");
            
            LocalDateTime currentTime = DateUtils.getCurrentVietnamTime();
            log.info(">>> [EscrowAutoResolveTask] Current time: {}", currentTime);
            
            // Tìm tất cả escrow records có status ESCROW_HOLD và endAt đã qua
            List<SystemWallet> escrowRecords = systemWalletRepository.findAll().stream()
                    .filter(sw -> sw.getStatus() == SystemWalletStatus.ESCROW_HOLD)
                    .filter(sw -> sw.getEndAt() != null && 
                            (sw.getEndAt().isBefore(currentTime) || sw.getEndAt().isEqual(currentTime)))
                    .toList();
            
            log.info(">>> [EscrowAutoResolveTask] Found {} escrow records to resolve (endAt <= currentTime)", escrowRecords.size());
            
            for (SystemWallet escrowRecord : escrowRecords) {
                try {
                    log.info(">>> [EscrowAutoResolveTask] Processing escrow record ID: {}, endAt: {}, balance: {}", 
                            escrowRecord.getId(), escrowRecord.getEndAt(), escrowRecord.getBalance());
                    
                    // Tìm seller wallet
                    Wallet sellerWallet = walletRepository.findByWalletId(escrowRecord.getSellerWalletId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Seller wallet not found with id: " + escrowRecord.getSellerWalletId()));
                    
                    BigDecimal balanceBefore = sellerWallet.getBalance();
                    BigDecimal transferAmount = escrowRecord.getBalance();
                    
                    // Tạo wallet transaction để ghi lại việc chuyển tiền
                    WalletTransaction walletTransaction = WalletTransaction.builder()
                            .wallet(sellerWallet)
                            .type(TransactionType.DEPOSIT)
                            .amount(transferAmount)
                            .balanceBefore(balanceBefore)
                            .status(TransactionStatus.SUCCESS)
                            .description("Tiền được chuyển tự động từ escrow cho đơn hàng #" + 
                                    (escrowRecord.getOrder() != null ? escrowRecord.getOrder().getId() : "N/A"))
                            .externalTransactionReference("ESCROW_AUTO_" + escrowRecord.getId())
                            .order(escrowRecord.getOrder())
                            .build();
                    walletTransactionRepository.save(walletTransaction);
                    
                    // Chuyển tiền từ escrow về ví người bán
                    sellerWallet.setBalance(balanceBefore.add(transferAmount));
                    walletRepository.save(sellerWallet);
                    
                    // Cập nhật status của escrow record thành IS_SOLVED
                    escrowRecord.setStatus(SystemWalletStatus.IS_SOLVED);
                    escrowRecord.setEndAt(DateUtils.getCurrentVietnamTime()); // Cập nhật endAt thành thời gian thực tế đã giải quyết
                    systemWalletRepository.save(escrowRecord);
                    
                    log.info(">>> [EscrowAutoResolveTask] Successfully resolved escrow record ID: {}, " +
                            "Transferred amount: {}, Seller wallet balance: {} -> {}", 
                            escrowRecord.getId(), transferAmount, balanceBefore, sellerWallet.getBalance());
                } catch (Exception e) {
                    log.error(">>> [EscrowAutoResolveTask] Error resolving escrow record ID: {}", 
                            escrowRecord.getId(), e);
                    // Tiếp tục xử lý các record khác nếu một record bị lỗi
                }
            }
            
            log.info(">>> [EscrowAutoResolveTask] Completed auto resolve escrow task. Processed {} records", 
                    escrowRecords.size());
        } catch (Exception e) {
            log.error(">>> [EscrowAutoResolveTask] Error in auto resolve escrow task", e);
        }
    }
}

