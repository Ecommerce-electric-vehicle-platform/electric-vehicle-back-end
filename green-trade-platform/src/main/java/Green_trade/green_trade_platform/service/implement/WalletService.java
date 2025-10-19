package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.Wallet;
import Green_trade.green_trade_platform.repository.BuyerRepository;
import Green_trade.green_trade_platform.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class WalletService {
    private final WalletRepository walletRepository;
    private final BuyerRepository buyerRepository;

    public WalletService(WalletRepository walletRepository, BuyerRepository buyerRepository) {
        this.walletRepository = walletRepository;
        this.buyerRepository = buyerRepository;
    }

    public Wallet createLocalWalletForBuyer(Buyer buyer) {
        Wallet wallet = Wallet.builder().buyer(buyer).build();
        return walletRepository.save(wallet);
    }

    public boolean isBuyerHasWallet(Buyer buyer) {
        boolean result = false;
        Optional<Wallet> walletOpt = walletRepository.findByBuyer(buyer);
        if(walletOpt.isPresent()) {
            result = true;
        }
        return result;
    }
}
