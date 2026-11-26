package ua.lpnu.studentrecords.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Нове значення налаштування.
 */
public record SettingUpdateRequest(@NotBlank(message = "Вкажіть значення") @Size(max = 255) String value) {
}
