package ua.lpnu.studentrecords.pattern.chain;

import ua.lpnu.studentrecords.pattern.scale.GradingScale;

/**
 * Перевіряє, що бал задано і він у межах 0..100.
 * <p>
 * <b>Шаблон Chain of Responsibility (роль ConcreteHandler).</b>
 */
public class ScoreRangeHandler extends GradeValidationHandler {

    @Override
    protected void check(GradeValidationContext context) {
        if (!GradingScale.INSTANCE.isValidScore(context.score())) {
            reject("Бал має бути цілим числом від " + GradingScale.MIN_SCORE
                    + " до " + GradingScale.MAX_SCORE);
        }
    }
}
