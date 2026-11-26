package ua.lpnu.studentrecords.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ua.lpnu.studentrecords.domain.enums.Role;
import ua.lpnu.studentrecords.service.AccountService;

import java.util.Optional;

/**
 * Доступ до поточного користувача з контексту безпеки та пов'язаних з ним студента / викладача.
 */
@Component
public class CurrentUserService {

    private final AccountService accountService;

    public CurrentUserService(AccountService accountService) {
        this.accountService = accountService;
    }

    public Optional<AuthenticatedUser> current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public AuthenticatedUser require() {
        return current().orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Потрібно увійти в систему"));
    }

    public boolean hasRole(Role role) {
        return current().map(user -> user.hasRole(role)).orElse(false);
    }

    /** Ідентифікатор студента поточного користувача (лише для ролі STUDENT). */
    public Optional<Long> studentId() {
        return current().filter(user -> user.hasRole(Role.STUDENT))
                .flatMap(user -> accountService.findStudentId(user.getId()));
    }

    /** Ідентифікатор викладача поточного користувача (лише для ролі TEACHER). */
    public Optional<Long> teacherId() {
        return current().filter(user -> user.hasRole(Role.TEACHER))
                .flatMap(user -> accountService.findTeacherId(user.getId()));
    }

    public Long requireStudentId() {
        return studentId().orElseThrow(() -> new AccessDeniedException("Обліковий запис не пов'язаний зі студентом"));
    }

    public Long requireTeacherId() {
        return teacherId().orElseThrow(() -> new AccessDeniedException("Обліковий запис не пов'язаний з викладачем"));
    }
}
