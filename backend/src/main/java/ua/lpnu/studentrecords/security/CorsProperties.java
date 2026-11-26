package ua.lpnu.studentrecords.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Дозволені джерела (origins) для CORS з {@code app.cors.allowed-origins}
 * (кілька значень – через кому, напр. {@code http://localhost:5173,http://localhost:3000}).
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {

    public CorsProperties {
        allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
    }
}
