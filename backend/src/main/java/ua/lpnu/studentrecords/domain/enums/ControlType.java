package ua.lpnu.studentrecords.domain.enums;

/**
 * Форма підсумкового контролю з дисципліни.
 */
public enum ControlType {
    /** Іспит. */
    EXAM,
    /** Залік (зараховано / не зараховано). */
    CREDIT,
    /** Диференційований залік. */
    DIFF_CREDIT;

    /** Чи оцінюється дисципліна за шкалою «зараховано / не зараховано». */
    public boolean isPassFail() {
        return this == CREDIT;
    }
}
