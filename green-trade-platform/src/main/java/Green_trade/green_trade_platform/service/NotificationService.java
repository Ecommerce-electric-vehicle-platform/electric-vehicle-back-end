package Green_trade.green_trade_platform.service;

import Green_trade.green_trade_platform.model.Notification;
import Green_trade.green_trade_platform.model.Seller;

import java.util.List;

public interface NotificationService {
    Notification createNotificationForSeller(Seller receiver, String title, String content);

    List<Notification> getNotifications(Long userId);

    Notification createNotification(Notification notification);

    void markAsRead(Long notificationId);
}

