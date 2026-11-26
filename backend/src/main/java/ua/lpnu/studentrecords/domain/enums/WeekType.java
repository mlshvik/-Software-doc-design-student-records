package ua.lpnu.studentrecords.domain.enums;

/**
 * Тип тижня: щотижня, чисельник (непарний) або знаменник (парний).
 */
public enum WeekType {
    ALL,
    ODD,
    EVEN;

    /** Чи перетинаються два заняття з такими типами тижнів. */
    public boolean overlaps(WeekType other) {
        return this == ALL || other == ALL || this == other;
    }
}
