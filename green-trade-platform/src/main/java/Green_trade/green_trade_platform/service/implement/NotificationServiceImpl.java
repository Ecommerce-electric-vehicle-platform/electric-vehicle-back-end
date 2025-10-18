package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.model.Notification;
import Green_trade.green_trade_platform.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl {
//    private final NotificationRepository notificationRepository;
//
//    public List<Notification> getNotifications(Long userId) {
//        return notificationRepository.findByReceiverOrderByCreatedAtDesc(userId);
//    }
//
//    public Notification createNotification(Notification notification) {
//        return notificationRepository.save(notification);
//    }
//
//    public void markAsRead(Long notificationId) {
//        Notification isRead = notificationRepository.findById(notificationId).orElseThrow();
//        isRead.setReadAt(LocalDateTime.now());
//        notificationRepository.save(isRead);
//    }
}
