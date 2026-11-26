package ua.lpnu.studentrecords.pattern.chain;

/**
 * Перевіряє, що оцінку виставляє саме той викладач, якому призначено дисципліну в групі.
 * Якщо викладача не вказано ({@code grader == null}), запис вносить адміністратор – перевірка
 * пропускається.
 * <p>
 * <b>Шаблон Chain of Responsibility (роль ConcreteHandler).</b>
 */
public class TeacherOwnsAssignmentHandler extends GradeValidationHandler {

    @Override
    protected void check(GradeValidationContext context) {
        if (context.grader() == null) {
            return;
        }
        if (!context.assignment().getTeacher().isSameAs(context.grader())) {
            reject("Викладач " + context.grader().getShortName()
                    + " не веде дисципліну «" + context.assignment().getSubject().getName()
                    + "» у групі " + context.assignment().getGroup().getName());
        }
    }
}
