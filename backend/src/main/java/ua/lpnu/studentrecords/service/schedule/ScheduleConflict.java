package ua.lpnu.studentrecords.service.schedule;

import ua.lpnu.studentrecords.domain.entity.ScheduleEntry;

/**
 * Знайдений конфлікт нового заняття з уже наявним у розкладі.
 *
 * @param type     причина конфлікту
 * @param existing заняття, з яким виник конфлікт
 * @param message  пояснення для користувача
 */
public record ScheduleConflict(ConflictType type, ScheduleEntry existing, String message) {
}
