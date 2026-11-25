package ua.lpnu.studentrecords.web.dto;

import java.time.LocalDateTime;
import ua.lpnu.studentrecords.domain.enums.GradeAttempt;

/**
 * Оцінка з переведенням у національну шкалу та ЄКТС.
 */
public record GradeResponse(Long id,
                            Long studentId,
                            String studentName,
                            Long assignmentId,
                            String subjectName,
                            Integer score,
                            GradeAttempt attempt,
                            String national,
                            String ects,
                            boolean passed,
                            LocalDateTime gradedAt,
                            String gradedBy) {
}
