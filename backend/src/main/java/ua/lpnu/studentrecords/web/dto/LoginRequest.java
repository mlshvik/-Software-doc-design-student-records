package ua.lpnu.studentrecords.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Запит на вхід у систему (логін і пароль).
 */
public record LoginRequest(@NotBlank(message = "Вкажіть логін") @Size(max = 50) String username,
                           @NotBlank(message = "Вкажіть пароль") @Size(max = 100) String password) {
}
