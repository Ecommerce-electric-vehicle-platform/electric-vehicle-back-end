package Green_trade.green_trade_platform.service;

import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.SystemWallet;
import Green_trade.green_trade_platform.model.Wallet;
import Green_trade.green_trade_platform.model.WalletTransaction;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface WalletService {
    Wallet createLocalWalletForBuyer(Buyer buyer);

    Wallet processDepositMoneyIntoWallet(Map<String, String> params);

    Wallet processDepositMoneyFromMoMo(Map<String, String> params);

    Wallet getWalletWithVnPayRequest(String params);

    Map<String, Object> handleSignPackageForSeller(Buyer buyer, double amount);

    boolean isBuyerHasWallet(Buyer buyer);

    Wallet handleBuyerRefund(SystemWallet systemWallet, double refundPercent, Wallet wallet, boolean isSeller, String orderCode);

    Wallet handleBuyerRefundForCancelledOrder(SystemWallet systemWallet, double refundPercent, Wallet wallet);

    Wallet findWalletById(Long buyerWalletId);

    Page<WalletTransaction> getTransactionHistory(Buyer buyer, int page, int size);

    Wallet withDrawMoney(Buyer buyer, double money);
}

