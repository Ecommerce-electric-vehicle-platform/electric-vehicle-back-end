package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.DisputeMapper;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.model.Dispute;
import Green_trade.green_trade_platform.model.Evidence;
import Green_trade.green_trade_platform.model.Notification;
import Green_trade.green_trade_platform.request.RaiseDisputeRequest;
import Green_trade.green_trade_platform.response.DisputeResponse;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.service.implement.DisputeServiceImpl;
import Green_trade.green_trade_platform.service.implement.EvidenceServiceImpl;
import Green_trade.green_trade_platform.service.implement.NotificationServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dispute")
@Slf4j
public class DisputeController {
    private final DisputeServiceImpl disputeService;
    private final EvidenceServiceImpl evidenceService;
    private final DisputeMapper disputeMapper;
    private final ResponseMapper responseMapper;
    private final NotificationServiceImpl notificationService;

    public DisputeController(DisputeServiceImpl disputeService,
                             EvidenceServiceImpl evidenceService,
                             DisputeMapper disputeMapper,
                             ResponseMapper responseMapper,
                             NotificationServiceImpl notificationService) {
        this.disputeService = disputeService;
        this.evidenceService = evidenceService;
        this.disputeMapper = disputeMapper;
        this.responseMapper = responseMapper;
        this.notificationService = notificationService;
    }

    @Operation(
            summary = "Raise a dispute for an order",
            description = "Allows a buyer to submit a dispute related to an order. " +
                    "The API receives dispute details and evidence pictures, " +
                    "saves them to the database, " +
                    "updates the dispute with associated evidences, " +
                    "and sends a notification to the seller about the disputed product."
    )
    @PostMapping("/raise-dispute")
    public ResponseEntity<RestResponse<?, ?>> raiseDispute(
            @ModelAttribute RaiseDisputeRequest request,
            @RequestPart("pictures") List<MultipartFile> files
    ) throws Exception {
        try {
            Dispute newDispute = disputeService.receiveDispute(request);
            List<Evidence> evidences = evidenceService.saveEvidence(files, newDispute);

            newDispute = disputeService.updateEvidencesForDispute(evidences, newDispute);
            log.info(">>> Passed update evidences for dispute");
//            Notification notification = notificationService.createNotificationForSeller(
//                    newDispute.getOrder().getPostProduct().getSeller(),
//                    "DISPUTE PRODUCT ALERT",
//                    "Your product has been disputed");
//            log.info(">>> Passed add notification");
            DisputeResponse responseData = disputeMapper.toDto(newDispute);
            RestResponse<DisputeResponse, Object> response = responseMapper.toDto(
                    true,
                    "RAISE DISPUTE SUCCESSFULLY",
                    responseData,
                    null
            );
            log.info(">>> Passed create response");
            return ResponseEntity.status(HttpStatus.OK.value()).body(response);
        } catch (Exception e) {
            log.info(">>> Error at raiseDisput: {}", e.getMessage());
            throw e;
        }
    }
}
