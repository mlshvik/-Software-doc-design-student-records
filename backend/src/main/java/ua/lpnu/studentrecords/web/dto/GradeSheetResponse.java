package ua.lpnu.studentrecords.web.dto;

import java.util.List;

/**
 * Відомість групи з дисципліни (для викладача).
 */
public record GradeSheetResponse(AssignmentResponse assignment,
                                 List<GradeSheetRow> rows) {
}
