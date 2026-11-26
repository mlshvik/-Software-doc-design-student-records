package ua.lpnu.studentrecords.pattern.state;

import ua.lpnu.studentrecords.domain.enums.StudentStatus;

import java.util.Set;

/**
 * Студент завершив навчання: кінцевий стан, жодні переходи і нові оцінки не допускаються.
 * <p>
 * <b>Шаблон State (роль ConcreteState).</b>
 */
public final class GraduatedState implements StudentState {

    GraduatedState() {
    }

    @Override
    public StudentStatus status() {
        return StudentStatus.GRADUATED;
    }

    @Override
    public String title() {
        return "Випускник";
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
        return Set.of();
    }
}
