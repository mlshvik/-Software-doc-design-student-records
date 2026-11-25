package ua.lpnu.studentrecords.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import ua.lpnu.studentrecords.web.exception.ProblemDetails;

import java.io.IOException;
import java.net.URI;

/**
 * Відповідь 401 для запиту без дійсного токена (замість стандартної HTML-сторінки входу).
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ProblemResponseWriter writer;

    public RestAuthenticationEntryPoint(ProblemResponseWriter writer) {
        this.writer = writer;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ProblemDetail problem = ProblemDetails.of(HttpStatus.UNAUTHORIZED, "unauthorized",
                "Потрібна автентифікація", "Увійдіть у систему: токен відсутній, недійсний або прострочений");
        problem.setInstance(URI.create(request.getRequestURI()));
        writer.write(response, problem);
    }
}
