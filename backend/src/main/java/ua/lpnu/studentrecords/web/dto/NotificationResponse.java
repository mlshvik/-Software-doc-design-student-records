package ua.lpnu.studentrecords.web.dto;

import java.time.LocalDateTime;

/**
 * Сповіщення користувача.
 */
public record NotificationResponse(Long id, String message, LocalDateTime createdAt, boolean read) {
}
