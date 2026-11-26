package ua.lpnu.studentrecords.service.schedule;

import ua.lpnu.studentrecords.domain.exception.BusinessException;

import java.util.List;

/**
 * Заняття не можна додати до розкладу: викладач, аудиторія або група вже зайняті.
 */
public class ScheduleConflictException extends BusinessException {

    private final transient List<ScheduleConflict> conflicts;

    public ScheduleConflictException(List<ScheduleConflict> conflicts) {
        super("Конфлікт у розкладі: " + String.join("; ",
                conflicts.stream().map(ScheduleConflict::message).toList()));
        this.conflicts = List.copyOf(conflicts);
    }

    public List<ScheduleConflict> getConflicts() {
        return conflicts;
    }
}
