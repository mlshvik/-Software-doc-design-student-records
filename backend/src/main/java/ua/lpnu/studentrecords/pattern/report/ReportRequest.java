package ua.lpnu.studentrecords.pattern.report;

/**
 * Параметри (фільтри) звіту. Будь-який параметр може бути {@code null} – тоді фільтр не застосовується.
 *
 * @param academicYear навчальний рік, напр. {@code 2025/2026}
 * @param semester     номер семестру 1..12
 * @param facultyId    факультет
 * @param course       курс 1..6
 */
public record ReportRequest(String academicYear, Integer semester, Long facultyId, Integer course) {

    public ReportRequest {
        academicYear = academicYear == null || academicYear.isBlank() ? null : academicYear.trim();
    }

    public static ReportRequest empty() {
        return new ReportRequest(null, null, null, null);
    }

    public boolean matchesYear(String year) {
        return academicYear == null || academicYear.equals(year);
    }

    public boolean matchesSemester(Integer value) {
        return semester == null || semester.equals(value);
    }

    public boolean matchesFaculty(Long value) {
        return facultyId == null || facultyId.equals(value);
    }

    public boolean matchesCourse(Integer value) {
        return course == null || course.equals(value);
    }
}
