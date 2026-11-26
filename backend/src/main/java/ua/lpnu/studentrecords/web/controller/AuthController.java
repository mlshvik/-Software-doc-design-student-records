package ua.lpnu.studentrecords.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.lpnu.studentrecords.security.AuthService;
import ua.lpnu.studentrecords.web.dto.LoginRequest;
import ua.lpnu.studentrecords.web.dto.LoginResponse;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Автентифікація", description = "Вхід у систему та отримання JWT")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Вхід у систему", description = "Повертає JWT-токен для заголовка Authorization: Bearer <token>")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
