package ua.lpnu.studentrecords.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Дані спеціальності.
 */
public record SpecialtyRequest(@NotBlank(message = "Вкажіть код спеціальності")
                               @Pattern(regexp = "[0-9A-Za-zА-Яа-яІіЇїЄє.]{1,10}", message = "Код спеціальності – до 10 символів, напр. 121")
                               String code,
                               @NotBlank(message = "Вкажіть назву спеціальності") @Size(max = 200) String name,
                               @NotNull(message = "Вкажіть факультет") Long facultyId) {
}
