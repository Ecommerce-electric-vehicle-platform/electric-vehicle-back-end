package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.exception.WalletNotFoundException;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.Wallet;
import Green_trade.green_trade_platform.model.WalletTransaction;
import Green_trade.green_trade_platform.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WalletServiceImpl {
    private final WalletRepository walletRepository;
    private final BuyerServiceImpl buyerService;
    private final WalletTransactionServiceImpl walletTransactionService;

    public WalletServiceImpl(WalletRepository walletRepository,
                             BuyerServiceImpl buyerService,
                             WalletTransactionServiceImpl walletTransactionService) {
        this.walletRepository = walletRepository;
        this.buyerService = buyerService;
        this.walletTransactionService = walletTransactionService;
    }

    public Wallet createLocalWalletForBuyer(Buyer buyer) {
        Wallet wallet = Wallet.builder().buyer(buyer).build();
        return walletRepository.save(wallet);
    }

    public Wallet processDepositMoneyIntoWallet(Map<String, String> params) {
        Wallet wallet = getWalletWithVnPayRequest(params.get("vnp_OrderInfo"));
        WalletTransaction walletTransaction = walletTransactionService.handleDepositIntoMoney(wallet, params);
        wallet.setBalance(wallet.getBalance().add(walletTransaction.getAmount()));
        walletRepository.save(wallet);
        return wallet;
    }

    public Wallet getWalletWithVnPayRequest(String params) {
        Buyer buyer = buyerService.getBuyerFromVnPayRequest(params);
        return  walletRepository.findByBuyer(buyer).orElseThrow(() -> new WalletNotFoundException("Người dùng chưa được tạo ví."));
    }

    public Map<String, Object> handleSignPackageForSeller(Buyer buyer, double amount) {

        Map<String, Object> result = new HashMap<>();

        Wallet wallet = walletRepository.findByBuyer(buyer).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy ví người dùng."));
        wallet.setBalance(wallet.getBalance().subtract(BigDecimal.valueOf(amount)));

        result.put("success", false);
        result.put("message", "Trừ tiền thành công.");
        result.put("data", wallet);

        return result;
    }
}
