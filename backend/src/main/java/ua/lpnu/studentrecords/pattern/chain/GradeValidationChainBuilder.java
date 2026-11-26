package ua.lpnu.studentrecords.pattern.chain;

import org.springframework.stereotype.Component;

/**
 * Збирає ланцюжок валідації оцінки в потрібному порядку:
 * {@code ScoreRangeHandler → StudentActiveHandler → TeacherOwnsAssignmentHandler → AttemptLimitHandler}.
 * <p>
 * Спершу виконуються найдешевші перевірки (лише поля запиту), потім – ті, що залежать
 * від стану студента та історії оцінок. Кожен виклик {@link #build()} створює новий
 * ланцюжок, тому обробники не поділяються між потоками.
 */
@Component
public class GradeValidationChainBuilder {

    /** @return перша ланка ланцюжка */
    public GradeValidationHandler build() {
        GradeValidationHandler head = new ScoreRangeHandler();
        head.setNext(new StudentActiveHandler())
                .setNext(new TeacherOwnsAssignmentHandler())
                .setNext(new AttemptLimitHandler());
        return head;
    }
}
