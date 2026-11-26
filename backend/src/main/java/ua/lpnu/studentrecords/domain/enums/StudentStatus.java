package ua.lpnu.studentrecords.domain.enums;

/**
 * Статус студента. Від статусу залежить поведінка студента в системі
 * (шаблон State, див. пакет {@code pattern.state}).
 */
public enum StudentStatus {
    /** Навчається. */
    ACTIVE,
    /** Академічна відпустка. */
    ACADEMIC_LEAVE,
    /** Відрахований. */
    EXPELLED,
    /** Випускник. */
    GRADUATED
}
