package ua.lpnu.studentrecords.pattern.state;

import ua.lpnu.studentrecords.domain.enums.StudentStatus;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Фабрика станів студента: повертає об'єкт-стан за значенням {@link StudentStatus},
 * яке зберігається в БД.
 * <p>
 * Стани не мають власних полів, тому кожен створюється один раз і використовується
 * повторно (легковаговики), а конструктори конкретних станів закриті в межах пакета.
 */
public final class StudentStateFactory {

    private static final Map<StudentStatus, StudentState> STATES = new EnumMap<>(StudentStatus.class);

    static {
        register(new ActiveState());
        register(new AcademicLeaveState());
        register(new ExpelledState());
        register(new GraduatedState());
    }

    private StudentStateFactory() {
    }

    private static void register(StudentState state) {
        STATES.put(state.status(), state);
    }

    /** Стан, що відповідає статусу студента. */
    public static StudentState forStatus(StudentStatus status) {
        Objects.requireNonNull(status, "status");
        return STATES.get(status);
    }
}
