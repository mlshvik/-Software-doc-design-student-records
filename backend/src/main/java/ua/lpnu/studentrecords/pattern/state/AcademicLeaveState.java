package ua.lpnu.studentrecords.pattern.state;

import ua.lpnu.studentrecords.domain.enums.StudentStatus;

import java.util.Set;

/**
 * Академічна відпустка: оцінки не виставляються, заняття не відвідуються; можливе повернення до навчання або відрахування.
 * <p>
 * <b>Шаблон State (роль ConcreteState).</b>
 */
public final class AcademicLeaveState implements StudentState {

    AcademicLeaveState() {
    }

    @Override
    public StudentStatus status() {
        return StudentStatus.ACADEMIC_LEAVE;
    }

    @Override
    public String title() {
        return "Академічна відпустка";
    }

    @Override
    public boolean canBeGraded() {
        return false;
    }

    @Override
    public boolean canAttendClasses() {
        return false;
    }

    @Override
    public Set<StudentStatus> allowedTransitions() {
        return Set.of(StudentStatus.ACTIVE, StudentStatus.EXPELLED);
    }
}
