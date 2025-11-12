package Green_trade.green_trade_platform.mapper;

import Green_trade.green_trade_platform.model.SystemWallet;
import Green_trade.green_trade_platform.response.SystemWalletResponse;
import org.springframework.stereotype.Component;

@Component
public class SystemWalletMapper {
    public SystemWalletResponse toDto(SystemWallet systemWallet) {
        if (systemWallet == null) {
            return null;
        }

        return SystemWalletResponse.builder()
                .id(systemWallet.getId())
                .buyerWalletId(systemWallet.getBuyerWalletId() != null ? systemWallet.getBuyerWalletId() : 0)
                .sellerWalletId(systemWallet.getSellerWalletId() != null ? systemWallet.getSellerWalletId() : 0)
                .concurrency(systemWallet.getConcurrency())
                .balance(systemWallet.getBalance())
                .shippingFee(systemWallet.getShippingFee())
                .status(systemWallet.getStatus())
                .createdAt(systemWallet.getCreatedAt())
                .build();
    }
}
