package ua.lpnu.studentrecords.pattern.state;

import ua.lpnu.studentrecords.domain.enums.StudentStatus;
import ua.lpnu.studentrecords.domain.exception.InvalidStatusTransitionException;

import java.util.Set;

/**
 * Стан студента.
 * <p>
 * <b>Шаблон State (роль State).</b> Поведінка студента залежить від статусу:
 * відрахованому чи випускнику не можна виставити оцінку, студент в академвідпустці
 * не відвідує заняття, з одних статусів можна перейти в інші, а з деяких – ні.
 * Замість розгалужень {@code if/switch} за статусом у сервісах кожен статус
 * інкапсульовано в окремому класі-стані, а {@code Student} делегує йому рішення
 * (див. {@code Student#changeStatus}).
 */
public interface StudentState {

    /** Статус, який представляє цей стан. */
    StudentStatus status();

    /** Назва статусу українською (для інтерфейсу користувача). */
    String title();

    /** Чи можна виставляти студенту оцінки. */
    boolean canBeGraded();

    /** Чи відвідує студент заняття (чи показувати йому розклад). */
    boolean canAttendClasses();

    /** Статуси, у які дозволено перейти з поточного. */
    Set<StudentStatus> allowedTransitions();

    /**
     * Перехід у новий статус з перевіркою допустимості.
     *
     * @return стан, що відповідає новому статусу
     * @throws InvalidStatusTransitionException якщо перехід заборонено
     */
    default StudentState transitionTo(StudentStatus target) {
        if (target == null || !allowedTransitions().contains(target)) {
            throw new InvalidStatusTransitionException(status(), target);
        }
        return StudentStateFactory.forStatus(target);
    }
}
