package ua.lpnu.studentrecords.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.lpnu.studentrecords.security.CurrentUserService;
import ua.lpnu.studentrecords.service.NotificationService;
import ua.lpnu.studentrecords.web.dto.NotificationResponse;

import java.util.List;
import java.util.Map;

/**
 * Сповіщення поточного користувача (наприклад, про нову оцінку – їх створює NotificationObserver).
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Сповіщення")
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserService currentUser;

    public NotificationController(NotificationService notificationService, CurrentUserService currentUser) {
        this.notificationService = notificationService;
        this.currentUser = currentUser;
    }

    @GetMapping("/me")
    @Operation(summary = "Мої сповіщення (новіші спочатку)")
    public List<NotificationResponse> mine() {
        return notificationService.forUser(currentUser.require().getId());
    }

    @GetMapping("/me/unread-count")
    @Operation(summary = "Кількість непрочитаних сповіщень")
    public Map<String, Long> unreadCount() {
        return Map.of("count", notificationService.unreadCount(currentUser.require().getId()));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Позначити сповіщення прочитаним")
    public NotificationResponse markRead(@PathVariable Long id) {
        return notificationService.markRead(id, currentUser.require().getId());
    }

    @PostMapping("/me/read-all")
    @Operation(summary = "Позначити всі сповіщення прочитаними")
    public Map<String, Integer> markAllRead() {
        return Map.of("updated", notificationService.markAllRead(currentUser.require().getId()));
    }
}
