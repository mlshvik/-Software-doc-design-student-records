package ua.lpnu.studentrecords.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import ua.lpnu.studentrecords.domain.enums.GradeAttempt;

/**
 * Виставлення оцінки. Якщо спробу не вказано, система визначає її сама (основна або перескладання).
 */
public record GradeRequest(@NotNull(message = "Вкажіть студента") Long studentId,
                           @NotNull(message = "Вкажіть призначення (дисципліну групи)") Long assignmentId,
                           @NotNull(message = "Вкажіть бал") @Min(value = 0, message = "Бал – від 0 до 100")
                           @Max(value = 100, message = "Бал – від 0 до 100") Integer score,
                           GradeAttempt attempt) {
}
