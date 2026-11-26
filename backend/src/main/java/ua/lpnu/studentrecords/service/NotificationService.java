package ua.lpnu.studentrecords.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lpnu.studentrecords.domain.entity.Notification;
import ua.lpnu.studentrecords.domain.exception.NotFoundException;
import ua.lpnu.studentrecords.repository.NotificationRepository;
import ua.lpnu.studentrecords.web.dto.NotificationResponse;
import ua.lpnu.studentrecords.web.mapper.AcademicMapper;

import java.util.List;

/**
 * Сповіщення користувачів (створюються спостерігачем {@code NotificationObserver}).
 * Кожен користувач бачить і змінює лише власні сповіщення.
 */
@Service
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<NotificationResponse> forUser(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId).stream()
                .map(AcademicMapper::toResponse)
                .toList();
    }

    public long unreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndReadFalse(userId);
    }

    @Transactional
    public NotificationResponse markRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findByIdAndRecipientId(notificationId, userId)
                .orElseThrow(() -> new NotFoundException("Сповіщення", notificationId));
        notification.markAsRead();
        return AcademicMapper.toResponse(notification);
    }

    @Transactional
    public int markAllRead(Long userId) {
        List<Notification> unread = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId).stream()
                .filter(n -> !n.isRead())
                .toList();
        unread.forEach(Notification::markAsRead);
        return unread.size();
    }
}
