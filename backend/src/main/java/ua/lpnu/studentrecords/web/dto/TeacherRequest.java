package ua.lpnu.studentrecords.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Дані викладача.
 */
public record TeacherRequest(@NotBlank(message = "Вкажіть прізвище") @Size(max = 100) String lastName,
                             @NotBlank(message = "Вкажіть ім'я") @Size(max = 100) String firstName,
                             @Size(max = 100) String middleName,
                             @Size(max = 100) String position,
                             @Size(max = 100) String academicDegree,
                             @Size(max = 200) String department,
                             @Email(message = "Некоректна адреса e-mail") @Size(max = 150) String email) {
}
