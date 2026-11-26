package ua.lpnu.studentrecords.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Призначення викладача на дисципліну в групі на семестр.
 */
public record AssignmentRequest(@NotNull(message = "Вкажіть дисципліну") Long subjectId,
                                @NotNull(message = "Вкажіть викладача") Long teacherId,
                                @NotNull(message = "Вкажіть групу") Long groupId,
                                @NotNull(message = "Вкажіть семестр") @Min(value = 1, message = "Семестр – від 1 до 12")
                                @Max(value = 12, message = "Семестр – від 1 до 12") Integer semester,
                                @NotBlank(message = "Вкажіть навчальний рік")
                                @Pattern(regexp = "\\d{4}/\\d{4}", message = "Навчальний рік у форматі 2025/2026") String academicYear) {
}
