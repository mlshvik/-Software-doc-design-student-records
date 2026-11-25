package ua.lpnu.studentrecords.web.dto;

import java.time.LocalDate;
import ua.lpnu.studentrecords.domain.enums.StudentStatus;

/**
 * Студент з групою і поточним статусом.
 */
public record StudentResponse(Long id,
                              String lastName,
                              String firstName,
                              String middleName,
                              String fullName,
                              String recordBookNumber,
                              String email,
                              String phone,
                              LocalDate birthDate,
                              Long groupId,
                              String groupName,
                              Integer course,
                              StudentStatus status,
                              String statusTitle,
                              String username) {
}
