package ua.lpnu.studentrecords.web.dto;

import ua.lpnu.studentrecords.domain.enums.Role;

/**
 * Поточний користувач: роль і пов'язаний студент або викладач.
 */
public record CurrentUserResponse(Long id,
                                  String username,
                                  Role role,
                                  String fullName,
                                  Long studentId,
                                  Long teacherId) {
}
