package ua.lpnu.studentrecords.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Видача і перевірка JWT-токенів (бібліотека jjwt, алгоритм HS256).
 * <p>
 * Токен містить логін (subject), ідентифікатор користувача і роль. Ключ підпису виводиться з
 * секрету через SHA-256, тож має рівно 256 біт незалежно від довжини рядка в конфігурації.
 */
@Service
public class JwtService {

    static final String CLAIM_USER_ID = "uid";
    static final String CLAIM_ROLE = "role";

    private final SecretKey key;
    private final Duration lifetime;
    private final Clock clock;

    @Autowired
    public JwtService(JwtProperties properties) {
        this(properties, Clock.systemUTC());
    }

    /** Конструктор для тестів: дозволяє зафіксувати час (перевірка простроченого токена). */
    JwtService(JwtProperties properties, Clock clock) {
        this.key = Keys.hmacShaKeyFor(sha256(properties.secret()));
        this.lifetime = Duration.ofMinutes(properties.expirationMinutes());
        this.clock = clock;
    }

    public IssuedToken issue(AuthenticatedUser user) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(lifetime);
        String token = Jwts.builder()
                .subject(user.getUsername())
                .claim(CLAIM_USER_ID, user.getId())
                .claim(CLAIM_ROLE, user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
        return new IssuedToken(token, expiresAt);
    }

    /**
     * Перевіряє підпис і строк дії токена.
     *
     * @return логін користувача або {@link Optional#empty()}, якщо токен недійсний
     */
    public Optional<String> extractUsername(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static byte[] sha256(String secret) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 недоступний", e);
        }
    }
}
