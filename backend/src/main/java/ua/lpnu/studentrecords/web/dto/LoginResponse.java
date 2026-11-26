package ua.lpnu.studentrecords.web.dto;

import java.time.Instant;

/**
 * Відповідь на успішний вхід: JWT-токен і дані користувача.
 */
public record LoginResponse(String token,
                            String tokenType,
                            Instant expiresAt,
                            CurrentUserResponse user) {
}
