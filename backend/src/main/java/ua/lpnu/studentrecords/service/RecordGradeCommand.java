package ua.lpnu.studentrecords.service;

import ua.lpnu.studentrecords.domain.enums.GradeAttempt;

/**
 * Команда «виставити оцінку».
 *
 * @param studentId    студент
 * @param assignmentId призначення (дисципліна в групі)
 * @param score        бал 0..100
 * @param attempt      спроба; {@code null} – визначити автоматично за історією оцінок
 * @param teacherId    викладач, що виставляє оцінку; {@code null} – запис вносить адміністратор
 */
public record RecordGradeCommand(Long studentId,
                                 Long assignmentId,
                                 Integer score,
                                 GradeAttempt attempt,
                                 Long teacherId) {
}
