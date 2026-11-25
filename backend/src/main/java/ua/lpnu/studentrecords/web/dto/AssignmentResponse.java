package ua.lpnu.studentrecords.web.dto;

import java.math.BigDecimal;
import ua.lpnu.studentrecords.domain.enums.ControlType;

/**
 * Призначення: дисципліна, викладач, група, семестр.
 */
public record AssignmentResponse(Long id,
                                 Long subjectId,
                                 String subjectName,
                                 ControlType controlType,
                                 BigDecimal credits,
                                 Long teacherId,
                                 String teacherName,
                                 Long groupId,
                                 String groupName,
                                 Integer course,
                                 Integer semester,
                                 String academicYear) {
}
