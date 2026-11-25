package ua.lpnu.studentrecords.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.DayOfWeek;
import ua.lpnu.studentrecords.domain.enums.LessonType;
import ua.lpnu.studentrecords.domain.enums.WeekType;

/**
 * Заняття в розкладі.
 */
public record ScheduleEntryRequest(@NotNull(message = "Вкажіть призначення (дисципліну групи)") Long assignmentId,
                                   @NotNull(message = "Вкажіть день тижня") DayOfWeek dayOfWeek,
                                   @NotNull(message = "Вкажіть номер пари") @Min(value = 1, message = "Пара – від 1 до 8")
                                   @Max(value = 8, message = "Пара – від 1 до 8") Integer pairNumber,
                                   WeekType weekType,
                                   @NotBlank(message = "Вкажіть аудиторію") @Size(max = 20) String classroom,
                                   @NotNull(message = "Вкажіть тип заняття") LessonType lessonType) {
}
