package ua.lpnu.studentrecords.pattern.chain;

import ua.lpnu.studentrecords.domain.entity.Student;

/**
 * Перевіряє, що студента можна оцінювати: його стан це дозволяє (шаблон State –
 * відрахованому, випускнику чи студенту в академвідпустці оцінку не виставити)
 * і він навчається в групі, якій призначено дисципліну.
 * <p>
 * <b>Шаблон Chain of Responsibility (роль ConcreteHandler).</b>
 */
public class StudentActiveHandler extends GradeValidationHandler {

    @Override
    protected void check(GradeValidationContext context) {
        Student student = context.student();
        if (!student.canBeGraded()) {
            reject("Студенту " + student.getShortName() + " не можна виставити оцінку: статус «"
                    + student.getState().title() + "»");
        }
        if (!student.getGroup().isSameAs(context.assignment().getGroup())) {
            reject("Студент " + student.getShortName() + " не навчається в групі "
                    + context.assignment().getGroup().getName());
        }
    }
}
