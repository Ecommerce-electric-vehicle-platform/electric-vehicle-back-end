package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequiredArgsConstructor
public class NotificationSocketController {
    private final SimpMessagingTemplate messagingTemplate;

    // Called by services when new notifications are created
    public void sendNotificationToUser(Notification notification) {
        String destination = "/queue/notifications/" + notification.getReceiverId();
        messagingTemplate.convertAndSend(destination, notification);
    }
}
