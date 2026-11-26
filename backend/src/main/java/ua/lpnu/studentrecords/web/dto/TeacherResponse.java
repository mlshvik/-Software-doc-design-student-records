package ua.lpnu.studentrecords.web.dto;

/**
 * Викладач.
 */
public record TeacherResponse(Long id,
                              String lastName,
                              String firstName,
                              String middleName,
                              String fullName,
                              String shortName,
                              String position,
                              String academicDegree,
                              String department,
                              String email,
                              String username) {
}
