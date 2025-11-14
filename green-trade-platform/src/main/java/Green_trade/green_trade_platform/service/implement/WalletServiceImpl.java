package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.exception.WalletNotFoundException;
import Green_trade.green_trade_platform.model.*;
import Green_trade.green_trade_platform.service.WalletService;
import Green_trade.green_trade_platform.repository.WalletRepository;
import Green_trade.green_trade_platform.repository.WalletTransactionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;
    private final BuyerServiceImpl buyerService;
    private final WalletTransactionServiceImpl walletTransactionService;
    private final WalletTransactionRepository walletTransactionRepository;

    public WalletServiceImpl(
            WalletRepository walletRepository,
            @Lazy BuyerServiceImpl buyerService, // tránh vòng lặp dependency
            WalletTransactionServiceImpl walletTransactionService,
            WalletTransactionRepository walletTransactionRepository) {
        this.walletRepository = walletRepository;
        this.buyerService = buyerService;
        this.walletTransactionService = walletTransactionService;
        this.walletTransactionRepository = walletTransactionRepository;
    }

    public Wallet createLocalWalletForBuyer(Buyer buyer) {
        Wallet wallet = Wallet.builder().buyer(buyer).build();
        return walletRepository.save(wallet);
    }

    public Wallet processDepositMoneyIntoWallet(Map<String, String> params) {
        String txnRef = params.get("vnp_TxnRef");

        if (walletTransactionRepository.existsByExternalTransactionReference(txnRef)) {
            log.warn(">>> [Duplicate Transaction] TxnRef {} already processed", txnRef);
            return walletTransactionRepository.findByExternalTransactionReference(txnRef)
                    .map(WalletTransaction::getWallet)
                    .orElse(null);
        }
        Wallet wallet = getWalletWithVnPayRequest(params.get("vnp_OrderInfo"));
        WalletTransaction walletTransaction = walletTransactionService.handleDepositIntoMoney(wallet, params);
        wallet.setBalance(wallet.getBalance().add(walletTransaction.getAmount()));
        walletRepository.save(wallet);
        return wallet;
    }

    public Wallet getWalletWithVnPayRequest(String params) {
        Buyer buyer = buyerService.getBuyerFromVnPayRequest(params);
        return walletRepository.findByBuyer(buyer).orElseThrow(() -> new WalletNotFoundException("Người dùng chưa được tạo ví."));
    }

    public Wallet processDepositMoneyFromMoMo(Map<String, String> params) {
        log.info(">>> [Wallet MoMo] Processing deposit with params: {}", params);
        
        String orderId = params.get("orderId");
        if (orderId == null || orderId.trim().isEmpty()) {
            log.error(">>> [Wallet MoMo] Missing orderId in params");
            throw new IllegalArgumentException("Missing orderId from MoMo callback");
        }

        if (walletTransactionRepository.existsByExternalTransactionReference(orderId)) {
            log.warn(">>> [Duplicate Transaction] OrderId {} already processed", orderId);
            return walletTransactionRepository.findByExternalTransactionReference(orderId)
                    .map(WalletTransaction::getWallet)
                    .orElseThrow(() -> new WalletNotFoundException("Transaction already processed but wallet not found"));
        }
        
        // Lấy buyerId từ orderInfo (format: "buyerId : username nạp tiền vào ví.")
        String orderInfo = params.get("orderInfo");
        if (orderInfo == null || orderInfo.trim().isEmpty()) {
            log.error(">>> [Wallet MoMo] Missing orderInfo in params");
            throw new IllegalArgumentException("Missing orderInfo from MoMo callback");
        }
        
        String buyerIdStr = orderInfo.contains(":") 
            ? orderInfo.split(":")[0].trim() 
            : null;
        
        if (buyerIdStr == null || buyerIdStr.trim().isEmpty()) {
            log.error(">>> [Wallet MoMo] Invalid orderInfo format: {}", orderInfo);
            throw new IllegalArgumentException("Invalid orderInfo format from MoMo: " + orderInfo);
        }
        
        try {
            Long buyerId = Long.parseLong(buyerIdStr);
            log.info(">>> [Wallet MoMo] Parsed buyerId: {}", buyerId);
            
            Buyer buyer = buyerService.findBuyerById(buyerId);
            if (buyer == null) {
                log.error(">>> [Wallet MoMo] Buyer not found with id: {}", buyerId);
                throw new IllegalArgumentException("Buyer not found with id: " + buyerId);
            }
            
            Wallet wallet = walletRepository.findByBuyer(buyer).orElseThrow(() -> {
                log.error(">>> [Wallet MoMo] Wallet not found for buyer: {}", buyerId);
                return new WalletNotFoundException("Người dùng chưa được tạo ví.");
            });
            
            log.info(">>> [Wallet MoMo] Found wallet: {}", wallet.getWalletId());
            
            // MoMo gửi amount trực tiếp (không nhân với 100), nên sử dụng hàm riêng cho MoMo
            String amountStr = params.get("amount");
            if (amountStr == null || amountStr.trim().isEmpty()) {
                log.error(">>> [Wallet MoMo] Missing amount in params");
                throw new IllegalArgumentException("Missing amount from MoMo callback");
            }
            
            log.info(">>> [Wallet MoMo] Creating transaction with MoMo params: {}", params);
            
            WalletTransaction walletTransaction = walletTransactionService.handleDepositIntoMoneyFromMoMo(wallet, params);
            wallet.setBalance(wallet.getBalance().add(walletTransaction.getAmount()));
            wallet = walletRepository.save(wallet);
            
            log.info(">>> [Wallet MoMo] Deposit successful. New balance: {}", wallet.getBalance());
            return wallet;
            
        } catch (NumberFormatException e) {
            log.error(">>> [Wallet MoMo] Invalid buyerId format: {}", buyerIdStr, e);
            throw new IllegalArgumentException("Invalid buyerId format: " + buyerIdStr, e);
        } catch (Exception e) {
            log.error(">>> [Wallet MoMo] Error processing deposit: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Map<String, Object> handleSignPackageForSeller(Buyer buyer, double amount) {

        Map<String, Object> result = new HashMap<>();

        Wallet wallet = walletRepository.findByBuyer(buyer).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy ví người dùng."));

        walletTransactionService.handleSignPackageForSeller(wallet, amount);
        wallet.setBalance(wallet.getBalance().subtract(BigDecimal.valueOf(amount)));

        result.put("success", false);
        result.put("message", "Trừ tiền thành công.");
        result.put("data", wallet);

        return result;
    }

    public boolean isBuyerHasWallet(Buyer buyer) {
        boolean result = false;
        Optional<Wallet> walletOpt = walletRepository.findByBuyer(buyer);
        if (walletOpt.isPresent()) {
            result = true;
        }
        return result;
    }

    public Wallet handleBuyerRefund(SystemWallet systemWallet, double refundPercent, Wallet wallet, boolean isSeller, String orderCode) {
        BigDecimal systemBalance = systemWallet.getBalance();
        BigDecimal money = systemBalance
                .multiply(BigDecimal.valueOf(refundPercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        Order order = systemWallet.getOrder(); // Lấy order từ systemWallet
        if (!isSeller) {
            WalletTransaction refundTransaction = walletTransactionService.handleRefundMoney(wallet, money, true, "Refund money from dispute" + orderCode, order);
            log.info(">>> [Wallet Service] Balance before refunding money for buyer: {}", wallet.getBalance());
            wallet.setBalance(wallet.getBalance().add(money));
            wallet = walletRepository.save(wallet);
            log.info(">>> [Wallet Service] Balance after refunding money for buyer: {}", wallet.getBalance());
        } else {
            WalletTransaction refundTransaction = walletTransactionService.handleRefundMoney(wallet, money, false, "Refund money from dispute" + orderCode, order);
            log.info(">>> [Wallet Service] Balance before refunding money for seller: {}", wallet.getBalance());
            wallet.setBalance(wallet.getBalance().add(money));
            wallet = walletRepository.save(wallet);
            log.info(">>> [Wallet Service] Balance after refunding money for seller: {}", wallet.getBalance());
        }
        return wallet;
    }

    public Wallet handleBuyerRefundForCancelledOrder(SystemWallet systemWallet, double refundPercent, Wallet wallet) {
        BigDecimal systemBalance = systemWallet.getBalance();
        BigDecimal shippingFee = systemWallet.getOrder().getShippingFee();
        BigDecimal money = systemBalance
                .multiply(BigDecimal.valueOf(refundPercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP).add(shippingFee);
        Order order = systemWallet.getOrder(); // Lấy order từ systemWallet
        WalletTransaction refundTransaction = walletTransactionService.handleRefundMoney(wallet, money, true, "REFUNDED FROM CANCELED ORDER", order);
        log.info(">>> [Wallet Service] Balance before refunding money for buyer: {}", wallet.getBalance());
        wallet.setBalance(wallet.getBalance().add(money));
        wallet = walletRepository.save(wallet);
        log.info(">>> [Wallet Service] Balance after refunding money for buyer: {}", wallet.getBalance());
        return wallet;
    }

    public Wallet findWalletById(Long buyerWalletId) {
        return walletRepository.findByWalletId(buyerWalletId).orElseThrow(
                () -> new IllegalArgumentException("Can not find wallet with this wallet id: " + buyerWalletId)
        );
    }

    public Page<WalletTransaction> getTransactionHistory(Buyer buyer, int page, int size) {
        Wallet wallet = buyer.getWallet();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return walletTransactionRepository.findByWallet(wallet, pageable);
    }

    public Wallet withDrawMoney(Buyer buyer, double money) {
        Wallet wallet = buyer.getWallet();

        WalletTransaction walletTransaction = walletTransactionService.handleWithDrawMoney(wallet, money);
        wallet.setBalance(wallet.getBalance().subtract(BigDecimal.valueOf(money)));
        return walletRepository.save(wallet);
    }
}
