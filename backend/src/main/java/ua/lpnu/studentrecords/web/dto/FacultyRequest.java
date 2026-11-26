package ua.lpnu.studentrecords.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Дані факультету (інституту) для створення або редагування.
 */
public record FacultyRequest(@NotBlank(message = "Вкажіть назву факультету") @Size(max = 200) String name,
                             @NotBlank(message = "Вкажіть скорочену назву") @Size(max = 20) String shortName) {
}
