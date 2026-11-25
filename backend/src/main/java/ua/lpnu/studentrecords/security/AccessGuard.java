package ua.lpnu.studentrecords.security;

import org.springframework.stereotype.Component;
import ua.lpnu.studentrecords.domain.enums.Role;
import ua.lpnu.studentrecords.service.TeachingAssignmentService;

/**
 * Перевірки доступу до конкретних записів, які використовуються у виразах {@code @PreAuthorize}:
 * <pre>{@code @PreAuthorize("@access.canViewStudent(#id)")}</pre>
 * Роль сама по собі не дає доступу до чужих даних: студент бачить лише себе,
 * викладач змінює лише оцінки з дисциплін, які він веде.
 */
@Component("access")
public class AccessGuard {

    private final CurrentUserService currentUser;
    private final TeachingAssignmentService assignmentService;

    public AccessGuard(CurrentUserService currentUser, TeachingAssignmentService assignmentService) {
        this.currentUser = currentUser;
        this.assignmentService = assignmentService;
    }

    /** Адміністратор і викладач бачать будь-якого студента, студент – лише себе. */
    public boolean canViewStudent(Long studentId) {
        if (currentUser.hasRole(Role.ADMIN) || currentUser.hasRole(Role.TEACHER)) {
            return true;
        }
        return isCurrentStudent(studentId);
    }

    public boolean isCurrentStudent(Long studentId) {
        return studentId != null && currentUser.studentId().map(studentId::equals).orElse(false);
    }

    /** Адміністратор має доступ до всіх призначень, викладач – лише до своїх. */
    public boolean canManageAssignment(Long assignmentId) {
        if (currentUser.hasRole(Role.ADMIN)) {
            return true;
        }
        return currentUser.teacherId()
                .map(teacherId -> assignmentService.isOwnedBy(assignmentId, teacherId))
                .orElse(false);
    }
}
