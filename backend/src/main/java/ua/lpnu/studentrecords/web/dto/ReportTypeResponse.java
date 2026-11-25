package ua.lpnu.studentrecords.web.dto;

import ua.lpnu.studentrecords.pattern.report.ReportType;

/**
 * Доступний тип звіту: ідентифікатор для URL і назва.
 */
public record ReportTypeResponse(ReportType type, String slug, String title) {

    public static ReportTypeResponse of(ReportType type) {
        return new ReportTypeResponse(type, type.slug(), type.title());
    }
}
