package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.mapper.DisputeMapper;
import Green_trade.green_trade_platform.mapper.ResponseMapper;
import Green_trade.green_trade_platform.model.Admin;
import Green_trade.green_trade_platform.model.Dispute;
import Green_trade.green_trade_platform.model.Evidence;
import Green_trade.green_trade_platform.model.Notification;
import Green_trade.green_trade_platform.request.RaiseDisputeRequest;
import Green_trade.green_trade_platform.request.RefundResolveRequest;
import Green_trade.green_trade_platform.request.ResolveDisputeRequest;
import Green_trade.green_trade_platform.response.DisputeResponse;
import Green_trade.green_trade_platform.response.RestResponse;
import Green_trade.green_trade_platform.service.implement.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/dispute")
@Slf4j
@AllArgsConstructor
public class DisputeController {
    private final DisputeServiceImpl disputeService;
    private final EvidenceServiceImpl evidenceService;
    private final DisputeMapper disputeMapper;
    private final ResponseMapper responseMapper;
    private final NotificationServiceImpl notificationService;
    private final AdminServiceImpl adminService;
    private final NotificationSocketController notificationSocketController;
    private final SystemWalletServiceImpl systemWalletService;

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

    @GetMapping("")
    public ResponseEntity<?> getDisputes(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        log.info(">>> [Dispute controller]: getDisputes");
        try {
            Page<DisputeResponse> disputes = disputeService.getAllDispute(page, size);
            Map<String, Object> data = new HashMap<>();
            data.put("dispute", disputes.getContent());
            data.put("currentPage",disputes.getNumber());
            data.put("totalElements", disputes.getTotalElements());
            data.put("totalPages", disputes.getTotalPages());
            log.info(">>> Get disputes successfully: {}", disputes.getTotalElements());

            return ResponseEntity.ok(responseMapper.toDto(
                    true,
                    "GET ALL PENDING DISPUTE SUCCESSFULLY.",
                    data, null));
        } catch (Exception e) {
            log.info(">>> Exception occure getDisputes: {}", e.getMessage());
            return ResponseEntity.ok(responseMapper.toDto(
                    false,
                    "GET ALL PENDING DISPUTE FAILED.",
                    null, e));
        }
    }

    @PostMapping("/resolve")
    public ResponseEntity<?> handleDispute(@RequestBody ResolveDisputeRequest request) {
        try {
            Admin admin = adminService.getCurrentUser();
            Notification notification = disputeService.handlePendingDispute(admin, request);
            notificationSocketController.sendNotificationToUser(notification);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/{disputeId}")
    public ResponseEntity<?> getDisputeInfo(@PathVariable(name = "disputeId") long disputeId) {
        Dispute dispute = disputeService.getDisputeInfo(disputeId);
        return ResponseEntity.ok(responseMapper.toDto(true,
                "GET DISPUTE INFOR SUCCESSFULLY.",
                disputeMapper.toDto(dispute),
                null));
    }

    @PostMapping("/refund/{return-percentage}")
    public ResponseEntity<?> handleRefund(@PathVariable(name = "return-percentage") double percent,
                                          @RequestBody RefundResolveRequest request) {
        systemWalletService.handleRefund(percent, request);
        return null;
    }

}
