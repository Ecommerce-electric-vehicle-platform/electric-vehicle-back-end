package Green_trade.green_trade_platform.mapper;

import Green_trade.green_trade_platform.enumerate.DisputeStatus;
import Green_trade.green_trade_platform.enumerate.TransactionType;
import Green_trade.green_trade_platform.model.Dispute;
import Green_trade.green_trade_platform.model.Evidence;
import Green_trade.green_trade_platform.model.Order;
import Green_trade.green_trade_platform.model.SystemWallet;
import Green_trade.green_trade_platform.model.WalletTransaction;
import Green_trade.green_trade_platform.repository.SystemWalletRepossitory;
import Green_trade.green_trade_platform.response.DisputeResponse;
import Green_trade.green_trade_platform.response.EvidenceResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class DisputeMapper {
    private final SystemWalletRepossitory systemWalletRepository;
    
    public DisputeResponse toDto(Dispute dispute) {
        List<Evidence> evidences = dispute.getEvidences();
        List<EvidenceResponse> evidenceResponses = evidences.stream().map(this::toEvidenDto).toList();
        
        DisputeResponse.DisputeResponseBuilder builder = DisputeResponse.builder()
                .disputeId(dispute.getId())
                .disputeCategoryId(dispute.getDisputeCategory().getId())
                .disputeCategoryName(dispute.getDisputeCategory().getTitle())
                .description(dispute.getDescription() != null ? dispute.getDescription() : dispute.getResolution())
                .resolution(dispute.getResolution())
                .decision(dispute.getDecision())
                .status(dispute.getStatus())
                .evidences(evidenceResponses);
        
        // Nếu dispute đã được giải quyết (status = ACCEPTED), tính toán thông tin tiền hoàn
        if (dispute.getStatus() == DisputeStatus.ACCEPTED && dispute.getOrder() != null) {
            try {
                Order order = dispute.getOrder();
                Optional<SystemWallet> systemWalletOpt = systemWalletRepository.findByOrder(order);
                
                if (systemWalletOpt.isPresent()) {
                    SystemWallet systemWallet = systemWalletOpt.get();
                    BigDecimal systemBalance = systemWallet.getBalance();
                    
                    // Tìm wallet transaction refund cho buyer
                    // Transaction có type = REFUND và description chứa "Refund money from dispute"
                    if (order.getWalletTransactions() != null && !order.getWalletTransactions().isEmpty()) {
                        Optional<WalletTransaction> refundTransaction = order.getWalletTransactions().stream()
                                .filter(wt -> wt.getType() == TransactionType.REFUND)
                                .filter(wt -> wt.getDescription() != null && 
                                        wt.getDescription().contains("Refund money from dispute"))
                                .findFirst();
                        
                        if (refundTransaction.isPresent()) {
                            BigDecimal refundAmount = refundTransaction.get().getAmount();
                            
                            // Tính refund percent dựa trên system wallet balance ban đầu
                            Double refundPercent = null;
                            if (systemBalance != null && systemBalance.compareTo(BigDecimal.ZERO) > 0) {
                                refundPercent = refundAmount
                                        .multiply(BigDecimal.valueOf(100))
                                        .divide(systemBalance, 2, RoundingMode.HALF_UP)
                                        .doubleValue();
                            }
                            
                            builder.refundAmount(refundAmount)
                                   .refundPercent(refundPercent);
                        }
                    }
                }
            } catch (Exception e) {
                // Nếu có lỗi khi tính toán, không set refund info
                // Không throw exception để không ảnh hưởng đến response chính
            }
        }
        
        return builder.build();
    }

    public EvidenceResponse toEvidenDto(Evidence evidence) {
        return EvidenceResponse.builder()
                .id(evidence.getId())
                .imageUrl(evidence.getImageUrl())
                .order(evidence.getOrderImage())
                .build();
    }
}
