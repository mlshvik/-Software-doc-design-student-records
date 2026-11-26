package ua.lpnu.studentrecords.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import ua.lpnu.studentrecords.web.exception.ProblemDetails;

import java.io.IOException;
import java.net.URI;

/**
 * Відповідь 403, коли автентифікований користувач не має потрібної ролі.
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ProblemResponseWriter writer;

    public RestAccessDeniedHandler(ProblemResponseWriter writer) {
        this.writer = writer;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        ProblemDetail problem = ProblemDetails.of(HttpStatus.FORBIDDEN, "forbidden", "Доступ заборонено",
                "Недостатньо прав для виконання операції");
        problem.setInstance(URI.create(request.getRequestURI()));
        writer.write(response, problem);
    }
}
