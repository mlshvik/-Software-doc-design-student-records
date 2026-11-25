package ua.lpnu.studentrecords.pattern.state;

import ua.lpnu.studentrecords.domain.enums.StudentStatus;

import java.util.Set;

/**
 * Студента відраховано: оцінки не виставляються; можливе лише поновлення (перехід у статус «навчається»).
 * <p>
 * <b>Шаблон State (роль ConcreteState).</b>
 */
public final class ExpelledState implements StudentState {

    ExpelledState() {
    }

    @Override
    public StudentStatus status() {
        return StudentStatus.EXPELLED;
    }

    @Override
    public String title() {
        return "Відрахований";
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
        return Set.of(StudentStatus.ACTIVE);
    }
}
