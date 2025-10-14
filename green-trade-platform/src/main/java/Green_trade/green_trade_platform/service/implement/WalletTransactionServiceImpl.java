package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.enumerate.TransactionStatus;
import Green_trade.green_trade_platform.enumerate.TransactionType;
import Green_trade.green_trade_platform.model.Wallet;
import Green_trade.green_trade_platform.model.WalletTransaction;
import Green_trade.green_trade_platform.repository.WalletTransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
public class WalletTransactionServiceImpl {
    private final WalletTransactionRepository walletTransactionRepository;

    public WalletTransactionServiceImpl(WalletTransactionRepository walletTransactionRepository) {
        this.walletTransactionRepository = walletTransactionRepository;
    }

    public WalletTransaction handleDepositIntoMoney(Wallet wallet, Map<String, String> params) {
        try {
            String amountTemp = params.get("vnp_Amount");
            long amount = Long.parseLong(amountTemp) / 100;
            log.info(">>> Amount of transaction: {}", amount);
            WalletTransaction walletTransaction = WalletTransaction.builder()
                    .type(TransactionType.DEPOSIT)
                    .amount(BigDecimal.valueOf(amount))
                    .balanceBefore(wallet.getBalance())
                    .status(TransactionStatus.SUCCESS)
                    .description("Nạp tiền vào ví người dùng")
                    .externalTransactionReference(params.get("vnp_TxnRef"))
                    .wallet(wallet)
                    .build();
            return walletTransactionRepository.save(walletTransaction);
        } catch (Exception e) {
            log.info(">>> Exception when deposit money into wallet: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public WalletTransaction handleSignPackageForSeller(Wallet wallet, double amount) {
        try {
           WalletTransaction walletTransaction = WalletTransaction.builder()
                   .wallet(wallet)
                   .type(TransactionType.SIGN_PACKAGE)
                   .amount(BigDecimal.valueOf(amount))
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
}
