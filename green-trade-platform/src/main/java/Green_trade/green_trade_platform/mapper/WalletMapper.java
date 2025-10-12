package Green_trade.green_trade_platform.mapper;

import Green_trade.green_trade_platform.model.Wallet;
import Green_trade.green_trade_platform.response.WalletResponse;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {
    public WalletResponse toDto(Wallet wallet) {
        return WalletResponse.builder()
                .balance(wallet.getBalance())
                .concurrency(wallet.getConcurrency())
                .build();
    }
}
