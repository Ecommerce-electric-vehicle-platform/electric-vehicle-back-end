package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.model.Notification;
import Green_trade.green_trade_platform.response.ApproveSellerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequiredArgsConstructor
@Slf4j
public class NotificationSocketController {
    private final SimpMessagingTemplate messagingTemplate;

    // Called by services when new notifications are created
    public void sendUpgradeNotificationToUser(ApproveSellerResponse notification) {
        log.info(">>> [Notification Socket Controller]: {}", notification);
        String destination = "/queue/notifications/" + notification.getSellerId();
        messagingTemplate.convertAndSend(destination, notification);
    }

    public void sendNotificationToUser(Notification notification) {
        log.info(">>> [Notification Socket Controller]: {}", notification);
        String destination = "/queue/notifications/" + notification.getReceiverId();
        messagingTemplate.convertAndSend(destination, notification);
    }
}
