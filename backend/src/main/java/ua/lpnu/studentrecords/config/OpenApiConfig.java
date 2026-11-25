package ua.lpnu.studentrecords.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Опис REST API для OpenAPI 3 / Swagger UI ({@code /swagger-ui.html}).
 * <p>
 * Усі операції, крім входу, вимагають JWT: у Swagger UI натисніть «Authorize» і вставте токен,
 * отриманий з {@code POST /api/auth/login}.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Система обліку студентів у ВНЗ – REST API",
                version = "0.4.0",
                description = "Облік студентів, викладачів, дисциплін, оцінок, академічних заборгованостей, "
                        + "розкладу та звітів. Курсова робота з дисципліни «Документування програмного "
                        + "забезпечення та шаблони проектування».",
                contact = @Contact(name = "Маліш Вікторія, ІР-43")),
        security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
@SecurityScheme(
        name = OpenApiConfig.BEARER_AUTH,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT-токен з відповіді POST /api/auth/login")
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";
}
