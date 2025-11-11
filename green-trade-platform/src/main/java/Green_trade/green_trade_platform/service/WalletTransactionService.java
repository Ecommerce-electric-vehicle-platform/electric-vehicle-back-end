package Green_trade.green_trade_platform.service;

import Green_trade.green_trade_platform.model.Wallet;
import Green_trade.green_trade_platform.model.WalletTransaction;

import java.math.BigDecimal;
import java.util.Map;

public interface WalletTransactionService {
    WalletTransaction handleDepositIntoMoney(Wallet wallet, Map<String, String> params);

    WalletTransaction handleSignPackageForSeller(Wallet wallet, double amount);

    WalletTransaction handleRefundMoney(Wallet wallet, BigDecimal money, boolean isRefund, String description);

    WalletTransaction handleWithDrawMoney(Wallet wallet, double money);
}

