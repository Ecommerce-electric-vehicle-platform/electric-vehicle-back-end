package Green_trade.green_trade_platform.service.implement;

import Green_trade.green_trade_platform.enumerate.AccountType;
import Green_trade.green_trade_platform.service.NotificationService;
import Green_trade.green_trade_platform.model.Notification;
import Green_trade.green_trade_platform.model.Seller;
import Green_trade.green_trade_platform.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import Green_trade.green_trade_platform.util.DateUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;

    public Notification createNotificationForSeller(Seller receiver, String title, String content) {
        Notification notification = Notification.builder()
                .receiverId(receiver.getSellerId())
                .type(AccountType.SELLER)
                .title(title)
                .content(content)
                .readAt(null)
                .build();
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotifications(Long userId) {
        return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(userId);
    }

    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    public void markAsRead(Long notificationId) {
        Notification isRead = notificationRepository.findById(notificationId).orElseThrow();
        isRead.setReadAt(DateUtils.convertToVietnamTime(LocalDateTime.now()));
        notificationRepository.save(isRead);
    }
}
