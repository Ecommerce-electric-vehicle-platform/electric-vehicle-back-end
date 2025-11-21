package Green_trade.green_trade_platform.mapper;

import Green_trade.green_trade_platform.enumerate.DisputeStatus;
import Green_trade.green_trade_platform.enumerate.TransactionType;
import Green_trade.green_trade_platform.model.Dispute;
import Green_trade.green_trade_platform.model.Evidence;
import Green_trade.green_trade_platform.model.Order;
import Green_trade.green_trade_platform.model.SystemWallet;
import Green_trade.green_trade_platform.model.WalletTransaction;
import Green_trade.green_trade_platform.repository.SystemWalletRepossitory;
import Green_trade.green_trade_platform.repository.WalletTransactionRepository;
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
    private final WalletTransactionRepository walletTransactionRepository;

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

                    // Tìm wallet transaction refund cho buyer từ database
                    // Query theo order id và type = REFUND
                    String descriptionPattern = "%Refund money from dispute%";
                    List<WalletTransaction> refundTransactions = walletTransactionRepository
                            .findRefundTransactionsByOrderAndType(
                                    order,
                                    TransactionType.REFUND,
                                    descriptionPattern
                            );

                    // Lọc transaction có description chứa "Refund money from dispute" và type = REFUND
                    // (chỉ lấy transaction của buyer, không phải seller - seller có type = DEPOSIT)
                    Optional<WalletTransaction> refundTransaction = refundTransactions.stream()
                            .filter(wt -> wt.getType() == TransactionType.REFUND)
                            .filter(wt -> wt.getDescription() != null &&
                                    wt.getDescription().startsWith("Refund money from dispute"))
                            .findFirst();

                    if (refundTransaction.isPresent()) {
                        BigDecimal refundAmount = refundTransaction.get().getAmount();

                        // Sử dụng refundPercent đã lưu trong dispute (nếu có)
                        // Nếu không có, tính toán từ refundAmount và systemBalance
                        Double refundPercent = dispute.getRefundPercent();

                        if (refundPercent == null && refundAmount != null && refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                            // Fallback: Tính refundPercent từ refundAmount và systemBalance hiện tại
                            // Giả sử systemBalance = originalBalance (vì không bị trừ sau refund)
                            // refundPercent = (refundAmount / systemBalance) * 100
                            if (systemBalance.compareTo(BigDecimal.ZERO) > 0) {
                                refundPercent = refundAmount
                                        .multiply(BigDecimal.valueOf(100))
                                        .divide(systemBalance, 2, RoundingMode.HALF_UP)
                                        .doubleValue();
                            }
                        }

                        builder.refundAmount(refundAmount)
                                .refundPercent(refundPercent);
                    }
                }
            } catch (Exception e) {
                // Nếu có lỗi khi tính toán, không set refund info
                // Không throw exception để không ảnh hưởng đến response chính
                // Log error để debug
                System.err.println("Error calculating refund info: " + e.getMessage());
                e.printStackTrace();
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
