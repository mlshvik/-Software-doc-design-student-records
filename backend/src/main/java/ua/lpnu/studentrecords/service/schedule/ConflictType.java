package ua.lpnu.studentrecords.service.schedule;

/**
 * Причина конфлікту в розкладі.
 */
public enum ConflictType {
    /** Викладач у цей час уже веде інше заняття. */
    TEACHER,
    /** Аудиторія в цей час уже зайнята. */
    CLASSROOM,
    /** Група в цей час уже має заняття. */
    GROUP
}
