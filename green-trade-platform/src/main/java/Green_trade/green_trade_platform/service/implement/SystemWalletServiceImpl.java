package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.enumerate.SystemWalletStatus;
import Green_trade.green_trade_platform.exception.SystemWalletException;
import Green_trade.green_trade_platform.model.Order;
import Green_trade.green_trade_platform.model.SystemWallet;
import Green_trade.green_trade_platform.repository.SystemWalletRepossitory;
import Green_trade.green_trade_platform.request.RefundResolveRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@AllArgsConstructor
public class SystemWalletServiceImpl {
    private final SystemWalletRepossitory systemWalletRepossitory;

    public void handleRefund(double percent, RefundResolveRequest request) {

    }

    public SystemWallet createEscrowRecord(Order order) {
        try {
            log.info(">>> [SystemWalletServiceImpl] the system came createEscrowRecord");
            SystemWallet escrowRecord = SystemWallet.builder()
                    .admin(null)
                    .buyerWalletId(order.getBuyer().getWallet().getWalletId())
                    .sellerWalletId(order.getPostProduct().getSeller().getBuyer().getWallet().getWalletId())
                    .concurrency("VND")
                    .balance(order.getPrice())
                    .status(SystemWalletStatus.ESCROW_HOLD)
                    .endAt(LocalDateTime.now().plusWeeks(2))
                    .build();
            log.info(">>> [SystemWalletServiceImpl] create new escrowRecord");
            return systemWalletRepossitory.save(escrowRecord);
        } catch(Exception e) {
            log.info(">>> [SystemWalletServiceImpl] Error at createEscrowRecord: {}", e.getMessage());
            throw new SystemWalletException();
        }
    }
}
