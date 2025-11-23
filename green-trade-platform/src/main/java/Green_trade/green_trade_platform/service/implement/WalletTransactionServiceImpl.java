package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.enumerate.TransactionStatus;
import Green_trade.green_trade_platform.service.WalletTransactionService;
import Green_trade.green_trade_platform.enumerate.TransactionType;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.Order;
import Green_trade.green_trade_platform.model.Wallet;
import Green_trade.green_trade_platform.model.WalletTransaction;
import Green_trade.green_trade_platform.repository.BuyerRepository;
import Green_trade.green_trade_platform.repository.WalletTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
@Slf4j
public class WalletTransactionServiceImpl implements WalletTransactionService {
    private final WalletTransactionRepository walletTransactionRepository;
    private final BuyerRepository buyerRepository;

    public WalletTransactionServiceImpl(WalletTransactionRepository walletTransactionRepository, BuyerRepository buyerRepository) {
        this.walletTransactionRepository = walletTransactionRepository;
        this.buyerRepository = buyerRepository;
    }

    public WalletTransaction handleDepositIntoMoney(Wallet wallet, Map<String, String> params) {
        try {
            String amountTemp = params.get("vnp_Amount");
            long amount = Long.parseLong(amountTemp);
            // VNPay gửi amount đã nhân với 100, nên cần chia lại cho 100
            BigDecimal amountDecimal = BigDecimal.valueOf(amount).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            log.info(">>> Amount of transaction (VNPay): {}", amountDecimal);
            WalletTransaction walletTransaction = WalletTransaction.builder()
                    .type(TransactionType.DEPOSIT)
                    .amount(amountDecimal)
                    .balanceBefore(wallet.getBalance())
                    .status(TransactionStatus.SUCCESS)
                    .description("Deposit money into user's wallet.")
                    .externalTransactionReference(params.get("vnp_TxnRef"))
                    .wallet(wallet)
                    .build();
            return walletTransactionRepository.save(walletTransaction);
        } catch (Exception e) {
            log.info(">>> Exception when deposit money into wallet: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public WalletTransaction handleDepositIntoMoneyFromMoMo(Wallet wallet, Map<String, String> params) {
        try {
            String amountTemp = params.get("amount");
            long amount = Long.parseLong(amountTemp);
            // MoMo gửi amount trực tiếp (không nhân với 100), nên không cần chia
            BigDecimal amountDecimal = BigDecimal.valueOf(amount);
            log.info(">>> Amount of transaction (MoMo): {}", amountDecimal);
            WalletTransaction walletTransaction = WalletTransaction.builder()
                    .type(TransactionType.DEPOSIT)
                    .amount(amountDecimal)
                    .balanceBefore(wallet.getBalance())
                    .status(TransactionStatus.SUCCESS)
                    .description("Deposit money into user's wallet from MoMo.")
                    .externalTransactionReference(params.get("orderId"))
                    .wallet(wallet)
                    .build();
            return walletTransactionRepository.save(walletTransaction);
        } catch (Exception e) {
            log.info(">>> Exception when deposit money into wallet from MoMo: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public WalletTransaction handleSignPackageForSeller(Wallet wallet, double amount) {
        try {
            WalletTransaction walletTransaction = WalletTransaction.builder()
                    .wallet(wallet)
                    .type(TransactionType.SIGN_PACKAGE)
                    .amount(BigDecimal.valueOf(amount).negate())
                    .balanceBefore(wallet.getBalance())
                    .status(TransactionStatus.SUCCESS)
                    .externalTransactionReference("None")
                    .build();
            return walletTransactionRepository.save(walletTransaction);
        } catch (Exception e) {
            log.info(">>> Exception when minus money from wallet to sign package: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public WalletTransaction handleRefundMoney(Wallet wallet, BigDecimal money, boolean isRefund, String description) {
        return handleRefundMoney(wallet, money, isRefund, description, null);
    }

    public WalletTransaction handleRefundMoney(Wallet wallet, BigDecimal money, boolean isRefund, String description, Order order) {
        log.info(">>> [Wallet Transaction Service] Handling refund money: Started.");
        try {
            log.info(">>> [Wallet Transaction Service] Started to create wallet transaction.");
            WalletTransaction walletTransaction = null;
            if (isRefund) {
                walletTransaction = WalletTransaction.builder()
                        .wallet(wallet)
                        .order(order)
                        .type(TransactionType.REFUND)
                        .amount(money)
                        .balanceBefore(wallet.getBalance())
                        .status(TransactionStatus.SUCCESS)
                        .externalTransactionReference("None")
                        .description(description)
                        .build();
            } else {
                String orderCodeSuffix = (order != null && order.getOrderCode() != null) 
                    ? " #" + order.getOrderCode() 
                    : "";
                walletTransaction = WalletTransaction.builder()
                        .wallet(wallet)
                        .order(order)
                        .type(TransactionType.DEPOSIT)
                        .amount(money)
                        .balanceBefore(wallet.getBalance())
                        .status(TransactionStatus.SUCCESS)
                        .externalTransactionReference("None")
                        .description("Nhận tiền từ đơn hàng" + orderCodeSuffix)
                        .build();
            }
            return walletTransactionRepository.save(walletTransaction);
        } catch (Exception e) {
            log.info(">>> [Wallet Transaction Service] Error occur when handle refund money: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public WalletTransaction handleWithDrawMoney(Wallet wallet, double money) {
        log.info(">>> [Wallet Transaction Service] Handling withdraw money: Started.");
        try {
            WalletTransaction walletTransaction = WalletTransaction.builder()
                    .wallet(wallet)
                    .type(TransactionType.WITHDRAW)
                    .amount(BigDecimal.valueOf(money).negate())
                    .balanceBefore(wallet.getBalance())
                    .status(TransactionStatus.SUCCESS)
                    .externalTransactionReference("None")
                    .description("Withdraw money from wallet")
                    .build();
            return walletTransactionRepository.save(walletTransaction);
        } catch (Exception e) {
            log.info(">>> [Wallet Transaction Service] Error occur when handle withdraw money: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
