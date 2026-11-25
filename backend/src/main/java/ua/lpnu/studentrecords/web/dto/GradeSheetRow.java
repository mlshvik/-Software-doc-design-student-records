package ua.lpnu.studentrecords.web.dto;

import java.util.List;
import ua.lpnu.studentrecords.domain.enums.GradeAttempt;
import ua.lpnu.studentrecords.domain.enums.StudentStatus;

/**
 * Рядок відомості: студент, його спроби і наступна допустима спроба.
 */
public record GradeSheetRow(Long studentId,
                            String studentName,
                            String recordBookNumber,
                            StudentStatus status,
                            boolean canBeGraded,
                            List<GradeResponse> grades,
                            Integer finalScore,
                            String finalEcts,
                            GradeAttempt nextAttempt) {
}
