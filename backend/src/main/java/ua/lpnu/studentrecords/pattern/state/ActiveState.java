package ua.lpnu.studentrecords.pattern.state;

import ua.lpnu.studentrecords.domain.enums.StudentStatus;

import java.util.Set;

/**
 * Студент навчається: може отримувати оцінки, відвідує заняття; може піти в академвідпустку, бути відрахованим або завершити навчання.
 * <p>
 * <b>Шаблон State (роль ConcreteState).</b>
 */
public final class ActiveState implements StudentState {

    ActiveState() {
    }

    @Override
    public StudentStatus status() {
        return StudentStatus.ACTIVE;
    }

    @Override
    public String title() {
        return "Навчається";
    }

    @Override
    public boolean canBeGraded() {
        return true;
    }

    @Override
    public boolean canAttendClasses() {
        return true;
    }

    @Override
    public Set<StudentStatus> allowedTransitions() {
        return Set.of(StudentStatus.ACADEMIC_LEAVE, StudentStatus.EXPELLED, StudentStatus.GRADUATED);
    }
}
