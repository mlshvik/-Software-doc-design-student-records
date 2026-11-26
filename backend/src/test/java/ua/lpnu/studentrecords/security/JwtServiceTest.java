package ua.lpnu.studentrecords.security;

import org.junit.jupiter.api.Test;
import ua.lpnu.studentrecords.domain.enums.Role;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тести видачі та перевірки JWT.
 */
class JwtServiceTest {

    private final JwtProperties properties = new JwtProperties("test-secret-for-unit-tests", 60);
    private final JwtService jwtService = new JwtService(properties);
    private final AuthenticatedUser user = new AuthenticatedUser(10L, "ir23101", "hash", Role.STUDENT, true);

    @Test
    void issuedTokenIsAccepted() {
        IssuedToken token = jwtService.issue(user);

        assertEquals(Optional.of("ir23101"), jwtService.extractUsername(token.token()));
        Duration lifetime = Duration.between(Instant.now(), token.expiresAt());
        assertTrue(lifetime.toMinutes() >= 59 && lifetime.toMinutes() <= 60, "строк дії – 60 хвилин");
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        String token = new JwtService(new JwtProperties("another-secret", 60)).issue(user).token();

        assertTrue(jwtService.extractUsername(token).isEmpty());
    }

    @Test
    void tamperedTokenIsRejected() {
        String[] parts = jwtService.issue(user).token().split("\\.");
        String forgedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"admin\",\"role\":\"ADMIN\"}".getBytes(StandardCharsets.UTF_8));
        String forged = parts[0] + "." + forgedPayload + "." + parts[2];

        assertTrue(jwtService.extractUsername(forged).isEmpty());
    }

    @Test
    void expiredTokenIsRejected() {
        Clock twoHoursAgo = Clock.fixed(Instant.now().minus(Duration.ofHours(2)), ZoneOffset.UTC);
        String token = new JwtService(properties, twoHoursAgo).issue(user).token();

        assertTrue(jwtService.extractUsername(token).isEmpty());
    }

    @Test
    void garbageIsRejected() {
        assertTrue(jwtService.extractUsername("not-a-jwt").isEmpty());
        assertTrue(jwtService.extractUsername("").isEmpty());
    }

    @Test
    void roleBecomesAuthority() {
        assertEquals("ROLE_STUDENT", user.getAuthorities().iterator().next().getAuthority());
    }
}
