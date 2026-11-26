package ua.lpnu.studentrecords.web.dto;

import ua.lpnu.studentrecords.service.schedule.ConflictType;

/**
 * Конфлікт розкладу: хто або що вже зайняте в цю пару.
 */
public record ScheduleConflictResponse(ConflictType type,
                                       Long existingEntryId,
                                       String message) {
}
