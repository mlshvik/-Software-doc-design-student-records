package ua.lpnu.studentrecords.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Налаштування JWT з {@code application.yml} ({@code app.jwt.*}).
 *
 * @param secret            секрет для підпису токенів (HMAC-SHA256); у продакшені задається змінною {@code JWT_SECRET}
 * @param expirationMinutes час життя токена у хвилинах
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long expirationMinutes) {

    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("Не задано app.jwt.secret");
        }
        if (expirationMinutes <= 0) {
            expirationMinutes = 480;
        }
    }
}
