package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.enumerate.WalletStatus;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.Wallet;
import Green_trade.green_trade_platform.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class WalletService {
    private WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }
    public Wallet createLocalWalletForBuyer(Buyer buyer) {
        Wallet w = new Wallet();
        w.setBuyer(buyer);
        walletRepository.save(w);
        return w;
    }
}
