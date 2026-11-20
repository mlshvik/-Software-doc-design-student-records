package ua.lpnu.studentrecords.domain.enums;

/**
 * Спроба складання дисципліни: основна та два перескладання.
 */
public enum GradeAttempt {
    /** Основна спроба під час сесії. */
    REGULAR,
    /** Перше перескладання (викладачу). */
    RETAKE_TEACHER,
    /** Друге перескладання (комісії). */
    RETAKE_COMMISSION;

    /** Наступна допустима спроба або {@code null}, якщо спроби вичерпано. */
    public GradeAttempt next() {
        return switch (this) {
            case REGULAR -> RETAKE_TEACHER;
            case RETAKE_TEACHER -> RETAKE_COMMISSION;
            case RETAKE_COMMISSION -> null;
        };
    }
}
