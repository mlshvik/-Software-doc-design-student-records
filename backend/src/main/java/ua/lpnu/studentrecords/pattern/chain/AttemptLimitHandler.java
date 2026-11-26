package ua.lpnu.studentrecords.pattern.chain;

import ua.lpnu.studentrecords.domain.entity.Grade;
import ua.lpnu.studentrecords.domain.enums.GradeAttempt;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Перевіряє порядок спроб: основна → перескладання викладачу → перескладання комісії.
 * Перескладання допускається лише після незадовільної попередньої спроби;
 * складену дисципліну повторно не оцінюють, а після комісії спроб більше немає.
 * <p>
 * <b>Шаблон Chain of Responsibility (роль ConcreteHandler).</b>
 */
public class AttemptLimitHandler extends GradeValidationHandler {

    @Override
    protected void check(GradeValidationContext context) {
        List<Grade> previous = context.previousGrades();
        if (previous.stream().anyMatch(Grade::isPassed)) {
            reject("Дисципліну вже складено, нову оцінку виставити не можна");
        }
        Optional<GradeAttempt> expected = expectedAttempt(previous);
        if (expected.isEmpty()) {
            reject("Усі спроби складання вичерпано (основна та два перескладання)");
        }
        if (context.attempt() != expected.get()) {
            reject("Очікувана спроба: " + expected.get() + ", отримано: " + context.attempt());
        }
    }

    /**
     * Наступна допустима спроба за історією оцінок.
     *
     * @return порожнє значення, якщо дисципліну складено або спроби вичерпано
     */
    public static Optional<GradeAttempt> expectedAttempt(List<Grade> previous) {
        if (previous.isEmpty()) {
            return Optional.of(GradeAttempt.REGULAR);
        }
        if (previous.stream().anyMatch(Grade::isPassed)) {
            return Optional.empty();
        }
        GradeAttempt last = previous.stream()
                .map(Grade::getAttempt)
                .max(Comparator.naturalOrder())
                .orElse(GradeAttempt.REGULAR);
        return Optional.ofNullable(last.next());
    }
}
