package ua.lpnu.studentrecords.pattern.chain;

import ua.lpnu.studentrecords.domain.entity.Grade;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.entity.Teacher;
import ua.lpnu.studentrecords.domain.entity.TeachingAssignment;
import ua.lpnu.studentrecords.domain.enums.GradeAttempt;

import java.util.List;

/**
 * Запит, який передається ланцюжком обробників валідації оцінки.
 *
 * @param student        студент, якому виставляється оцінка
 * @param assignment     призначення (дисципліна + викладач + група + семестр)
 * @param grader         викладач, що виставляє оцінку; {@code null} – запис вносить адміністратор
 * @param score          бал за 100-бальною шкалою
 * @param attempt        спроба (основна або перескладання)
 * @param previousGrades попередні оцінки студента з цього призначення
 */
public record GradeValidationContext(Student student,
                                     TeachingAssignment assignment,
                                     Teacher grader,
                                     Integer score,
                                     GradeAttempt attempt,
                                     List<Grade> previousGrades) {

    public GradeValidationContext {
        previousGrades = previousGrades == null ? List.of() : List.copyOf(previousGrades);
    }
}
