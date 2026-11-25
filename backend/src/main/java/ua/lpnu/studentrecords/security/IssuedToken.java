package ua.lpnu.studentrecords.security;

import java.time.Instant;

/**
 * Виданий JWT-токен і момент, коли він перестане діяти.
 */
public record IssuedToken(String token, Instant expiresAt) {
}
