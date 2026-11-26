package ua.lpnu.studentrecords.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Дані академічної групи.
 */
public record GroupRequest(@NotBlank(message = "Вкажіть назву групи") @Size(max = 20) String name,
                           @NotNull(message = "Вкажіть спеціальність") Long specialtyId,
                           @NotNull(message = "Вкажіть курс") @Min(value = 1, message = "Курс – від 1 до 6")
                           @Max(value = 6, message = "Курс – від 1 до 6") Integer course,
                           @NotNull(message = "Вкажіть рік вступу") @Min(2000) @Max(2100) Integer admissionYear) {
}
