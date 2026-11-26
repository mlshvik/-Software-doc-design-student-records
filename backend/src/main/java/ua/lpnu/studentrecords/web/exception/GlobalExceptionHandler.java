package ua.lpnu.studentrecords.web.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ua.lpnu.studentrecords.domain.exception.BusinessException;
import ua.lpnu.studentrecords.domain.exception.ConflictException;
import ua.lpnu.studentrecords.domain.exception.GradeValidationException;
import ua.lpnu.studentrecords.domain.exception.InvalidStatusTransitionException;
import ua.lpnu.studentrecords.domain.exception.NotFoundException;
import ua.lpnu.studentrecords.service.schedule.ScheduleConflictException;
import ua.lpnu.studentrecords.web.mapper.AcademicMapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Глобальна обробка винятків REST API.
 * <p>
 * Перетворює винятки бізнес-правил і помилки валідації на відповіді {@link ProblemDetail}
 * (RFC 7807) з українським описом і машинозчитуваним полем {@code code}:
 * <ul>
 *     <li>{@link NotFoundException} → 404;</li>
 *     <li>{@link GradeValidationException} (ланцюжок Chain of Responsibility) → 422 з назвою правила;</li>
 *     <li>{@link InvalidStatusTransitionException} (шаблон State), {@link ConflictException},
 *         {@link ScheduleConflictException} → 409;</li>
 *     <li>помилки Bean Validation і некоректний JSON → 400 зі списком полів;</li>
 *     <li>відмова в доступі → 403, неавтентифікований запит → 401.</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(NotFoundException ex) {
        return build(ProblemDetails.of(HttpStatus.NOT_FOUND, "not-found", "Запис не знайдено", ex.getMessage()));
    }

    @ExceptionHandler(GradeValidationException.class)
    public ResponseEntity<ProblemDetail> handleGradeValidation(GradeValidationException ex) {
        ProblemDetail problem = ProblemDetails.of(HttpStatus.UNPROCESSABLE_ENTITY, "grade-validation",
                "Оцінку не можна виставити", ex.getMessage());
        problem.setProperty("rule", ex.getRule());
        return build(problem);
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ProblemDetail> handleStatusTransition(InvalidStatusTransitionException ex) {
        ProblemDetail problem = ProblemDetails.of(HttpStatus.CONFLICT, "invalid-status-transition",
                "Недопустима зміна статусу", ex.getMessage());
        problem.setProperty("from", ex.getFrom());
        problem.setProperty("to", ex.getTo());
        return build(problem);
    }

    @ExceptionHandler(ScheduleConflictException.class)
    public ResponseEntity<ProblemDetail> handleScheduleConflict(ScheduleConflictException ex) {
        ProblemDetail problem = ProblemDetails.of(HttpStatus.CONFLICT, "schedule-conflict",
                "Конфлікт у розкладі", ex.getMessage());
        problem.setProperty("conflicts", ex.getConflicts().stream().map(AcademicMapper::toResponse).toList());
        return build(problem);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetail> handleConflict(ConflictException ex) {
        return build(ProblemDetails.of(HttpStatus.CONFLICT, "conflict", "Конфлікт даних", ex.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusiness(BusinessException ex) {
        return build(ProblemDetails.of(HttpStatus.UNPROCESSABLE_ENTITY, "business-rule",
                "Порушено бізнес-правило", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ex.getBindingResult().getGlobalErrors()
                .forEach(error -> errors.putIfAbsent(error.getObjectName(), error.getDefaultMessage()));
        ProblemDetail problem = ProblemDetails.of(HttpStatus.BAD_REQUEST, "validation",
                "Некоректні дані", "Перевірте правильність заповнення полів");
        problem.setProperty("errors", errors);
        return build(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                errors.putIfAbsent(violation.getPropertyPath().toString(), violation.getMessage()));
        ProblemDetail problem = ProblemDetails.of(HttpStatus.BAD_REQUEST, "validation",
                "Некоректні дані", "Перевірте правильність параметрів запиту");
        problem.setProperty("errors", errors);
        return build(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(ProblemDetails.of(HttpStatus.BAD_REQUEST, "malformed-request",
                "Некоректний запит", "Тіло запиту відсутнє або має неправильний формат JSON"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(ProblemDetails.of(HttpStatus.BAD_REQUEST, "malformed-request", "Некоректний запит",
                "Параметр «" + ex.getName() + "» має неправильне значення: " + ex.getValue()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
        return build(ProblemDetails.of(HttpStatus.BAD_REQUEST, "bad-request", "Некоректний запит", ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Порушення цілісності даних: {}", ex.getMostSpecificCause().getMessage());
        return build(ProblemDetails.of(HttpStatus.CONFLICT, "data-integrity", "Конфлікт даних",
                "Операцію не виконано: запис дублює наявний або використовується іншими даними"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex) {
        return build(ProblemDetails.of(HttpStatus.FORBIDDEN, "forbidden", "Доступ заборонено",
                "Недостатньо прав для виконання операції"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthentication(AuthenticationException ex) {
        String detail;
        if (ex instanceof BadCredentialsException) {
            detail = "Неправильний логін або пароль";
        } else if (ex instanceof DisabledException) {
            detail = "Обліковий запис заблоковано. Зверніться до адміністратора";
        } else {
            detail = "Потрібно увійти в систему";
        }
        return build(ProblemDetails.of(HttpStatus.UNAUTHORIZED, "unauthorized", "Потрібна автентифікація", detail));
    }

    /**
     * Стандартні помилки Spring MVC (метод не підтримується, ресурс не знайдено тощо) вже містять
     * статус і опис – повертаємо їх як є; решта – непередбачені помилки сервера (500).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleOther(Exception ex) {
        if (ex instanceof ErrorResponse errorResponse) {
            return ResponseEntity.status(errorResponse.getStatusCode()).body(errorResponse.getBody());
        }
        log.error("Непередбачена помилка", ex);
        return build(ProblemDetails.of(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error",
                "Внутрішня помилка сервера", "Сталася непередбачена помилка. Спробуйте пізніше."));
    }

    private static ResponseEntity<ProblemDetail> build(ProblemDetail problem) {
        return ResponseEntity.status(problem.getStatus()).body(problem);
    }
}
