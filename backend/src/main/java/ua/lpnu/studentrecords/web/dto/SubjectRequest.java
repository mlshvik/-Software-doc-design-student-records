package ua.lpnu.studentrecords.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import ua.lpnu.studentrecords.domain.enums.ControlType;

/**
 * Дані навчальної дисципліни.
 */
public record SubjectRequest(@NotBlank(message = "Вкажіть назву дисципліни") @Size(max = 200) String name,
                             @NotNull(message = "Вкажіть кількість кредитів ЄКТС")
                             @DecimalMin(value = "0.5", message = "Щонайменше 0.5 кредиту")
                             @DecimalMax(value = "30.0", message = "Не більше 30 кредитів") BigDecimal credits,
                             @NotNull(message = "Вкажіть кількість годин") @Min(1) @Max(900) Integer hours,
                             @NotNull(message = "Вкажіть форму контролю") ControlType controlType) {
}
