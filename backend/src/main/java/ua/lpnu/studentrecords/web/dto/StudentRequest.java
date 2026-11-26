package ua.lpnu.studentrecords.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Дані студента для створення або редагування (статус змінюється окремим запитом).
 */
public record StudentRequest(@NotBlank(message = "Вкажіть прізвище") @Size(max = 100) String lastName,
                             @NotBlank(message = "Вкажіть ім'я") @Size(max = 100) String firstName,
                             @Size(max = 100) String middleName,
                             @NotBlank(message = "Вкажіть номер залікової книжки") @Size(max = 20) String recordBookNumber,
                             @Email(message = "Некоректна адреса e-mail") @Size(max = 150) String email,
                             @Pattern(regexp = "^$|^\\+?[0-9 ()-]{7,20}$", message = "Некоректний номер телефону") String phone,
                             @Past(message = "Дата народження має бути в минулому") LocalDate birthDate,
                             @NotNull(message = "Вкажіть групу") Long groupId) {
}
