package ua.lpnu.studentrecords.pattern.report;

import java.util.Locale;

/**
 * Типи звітів, які вміє створювати {@link ReportGeneratorFactory}.
 * У URL використовується «kebab-case» назва: {@code /api/reports/group-performance}.
 */
public enum ReportType {

    /** Успішність за групами та курсами. */
    GROUP_PERFORMANCE("group-performance", "Успішність за групами та курсами"),
    /** Середні оцінки студентів по викладачах. */
    TEACHER_STATISTICS("teacher-statistics", "Статистика оцінювання по викладачах"),
    /** Відкриті академічні заборгованості. */
    ACADEMIC_DEBTS("academic-debts", "Академічні заборгованості");

    private final String slug;
    private final String title;

    ReportType(String slug, String title) {
        this.slug = slug;
        this.title = title;
    }

    public String slug() {
        return slug;
    }

    public String title() {
        return title;
    }

    /**
     * Розпізнає тип звіту за назвою з URL: приймає і {@code group-performance}, і {@code GROUP_PERFORMANCE}.
     *
     * @throws IllegalArgumentException якщо такого типу немає
     */
    public static ReportType fromValue(String value) {
        if (value != null) {
            String normalized = value.trim();
            for (ReportType type : values()) {
                if (type.slug.equalsIgnoreCase(normalized)
                        || type.name().equals(normalized.toUpperCase(Locale.ROOT).replace('-', '_'))) {
                    return type;
                }
            }
        }
        throw new IllegalArgumentException("Невідомий тип звіту: " + value);
    }
}
