package ua.lpnu.studentrecords.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.Instant;

/**
 * Фабрика відповідей про помилки у форматі RFC 7807 ({@link ProblemDetail}).
 * Використовується і обробником винятків контролерів, і фільтрами безпеки,
 * щоб клієнт завжди отримував однакову структуру помилки.
 */
public final class ProblemDetails {

    /** Базова адреса для поля {@code type}: {@code urn:student-records:problem:<code>}. */
    private static final String TYPE_PREFIX = "urn:student-records:problem:";

    private ProblemDetails() {
    }

    public static ProblemDetail of(HttpStatus status, String code, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(TYPE_PREFIX + code));
        problem.setTitle(title);
        problem.setProperty("code", code);
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }
}
