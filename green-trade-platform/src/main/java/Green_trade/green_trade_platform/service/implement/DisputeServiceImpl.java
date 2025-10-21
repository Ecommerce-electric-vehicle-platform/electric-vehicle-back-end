package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.model.Dispute;
import Green_trade.green_trade_platform.model.DisputeCategory;
import Green_trade.green_trade_platform.model.Evidence;
import Green_trade.green_trade_platform.repository.DisputeCategoryRepository;
import Green_trade.green_trade_platform.repository.DisputeRepository;
import Green_trade.green_trade_platform.request.RaiseDisputeRequest;
import Green_trade.green_trade_platform.service.DisputeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DisputeServiceImpl implements DisputeService {
    private final DisputeCategoryRepository disputeCategoryRepository;
    private final DisputeRepository disputeRepository;

    public DisputeServiceImpl(
            DisputeCategoryRepository disputeCategoryRepository,
            DisputeRepository disputeRepository) {
        this.disputeCategoryRepository = disputeCategoryRepository;
        this.disputeRepository = disputeRepository;
    }

    public Dispute updateEvidencesForDispute(List<Evidence> evidences, Dispute dispute) {
        dispute.setEvidences(evidences);
        return disputeRepository.save(dispute);
    }

    public Dispute receiveDispute(RaiseDisputeRequest request) throws Exception {
        try {
            DisputeCategory disputeCategory = disputeCategoryRepository.findById(request.getDisputeCategoryId())
                    .orElseThrow(() ->
                        new Exception("Dispute Category is not supported")
                    );
            Dispute newDispute = Dispute.builder()
                    .disputeCategory(disputeCategory)
                    .admin(null)
                    .evidences(null)
                    .decision("No Decision Yet")
                    .resolution("No Resolution Yet")
                    .resolutionType("Not Have Yet")
                    .status("PENDING")
                    .build();
            return disputeRepository.save(newDispute);
        } catch (Exception e) {
            throw e;
        }
    }
}
