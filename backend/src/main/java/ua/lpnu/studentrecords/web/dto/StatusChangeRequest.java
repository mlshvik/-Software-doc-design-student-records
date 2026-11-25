package ua.lpnu.studentrecords.web.dto;

import jakarta.validation.constraints.NotNull;
import ua.lpnu.studentrecords.domain.enums.StudentStatus;

/**
 * Запит на зміну статусу студента (перевіряється шаблоном State).
 */
public record StatusChangeRequest(@NotNull(message = "Вкажіть новий статус") StudentStatus status) {
}
