package ua.lpnu.studentrecords.web.dto;

import java.time.LocalDateTime;
import ua.lpnu.studentrecords.domain.enums.DebtStatus;

/**
 * Академічна заборгованість.
 */
public record DebtResponse(Long id,
                           Long studentId,
                           String studentName,
                           String groupName,
                           Long assignmentId,
                           String subjectName,
                           String teacherName,
                           Integer semester,
                           String academicYear,
                           DebtStatus status,
                           LocalDateTime openedAt,
                           LocalDateTime closedAt) {
}
