package Green_trade.green_trade_platform.controller;

import Green_trade.green_trade_platform.model.Admin;
import Green_trade.green_trade_platform.model.Buyer;
import Green_trade.green_trade_platform.model.Notification;
import Green_trade.green_trade_platform.service.implement.AdminServiceImpl;
import Green_trade.green_trade_platform.service.implement.BuyerServiceImpl;
import Green_trade.green_trade_platform.service.implement.NotificationServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationServiceImpl notificationService;
    private final NotificationSocketController socketController;
    private final BuyerServiceImpl buyerService;
    private final AdminServiceImpl adminService;

    // Get notifications for a user
    @GetMapping("")
    public ResponseEntity<List<Notification>> getAll() {
        Long receiverId = 0L;
        try {
            Buyer buyer = buyerService.getCurrentUser();
            receiverId = buyer.getBuyerId();
        } catch (Exception e) {
            Admin admin = adminService.getCurrentUser();
            receiverId = admin.getId();
        }
        return ResponseEntity.ok(notificationService.getNotifications(receiverId));
    }

    // Create a new notification (and push to user via WebSocket)
    @PostMapping("/new-notification")
    public ResponseEntity<Notification> create(@RequestBody Notification notification) {
        Notification saved = notificationService.createNotification(notification);
        socketController.sendNotificationToUser(saved);
        return ResponseEntity.ok(saved);
    }

    // Mark notification as read
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }
}
