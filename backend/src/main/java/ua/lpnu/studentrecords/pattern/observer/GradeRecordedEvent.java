package ua.lpnu.studentrecords.pattern.observer;

import ua.lpnu.studentrecords.domain.entity.Grade;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.entity.TeachingAssignment;

import java.util.Objects;

/**
 * Подія «виставлено нову оцінку», яку отримують спостерігачі.
 *
 * @param grade   збережена оцінка
 * @param student студент, якому виставлено оцінку
 */
public record GradeRecordedEvent(Grade grade, Student student) {

    public GradeRecordedEvent {
        Objects.requireNonNull(grade, "grade");
        Objects.requireNonNull(student, "student");
    }

    public TeachingAssignment assignment() {
        return grade.getAssignment();
    }

    /** Чи є оцінка позитивною. */
    public boolean passed() {
        return grade.isPassed();
    }
}
