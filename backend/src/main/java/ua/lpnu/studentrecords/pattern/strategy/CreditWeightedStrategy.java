package ua.lpnu.studentrecords.pattern.strategy;

import org.springframework.stereotype.Component;
import ua.lpnu.studentrecords.domain.entity.Grade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Середній бал, зважений на кредити ЄКТС: {@code Σ (бал × кредити) / Σ кредити}.
 * Дисципліна на 6 кредитів впливає на результат удвічі сильніше, ніж на 3 кредити.
 * <p>
 * <b>Шаблон Strategy (роль ConcreteStrategy).</b>
 */
@Component
public class CreditWeightedStrategy implements GpaCalculationStrategy {

    @Override
    public GpaStrategyType type() {
        return GpaStrategyType.CREDIT_WEIGHTED;
    }

    @Override
    public BigDecimal calculate(List<Grade> grades) {
        BigDecimal weightedSum = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;
        for (Grade grade : FinalGradeSelector.selectFinal(grades)) {
            BigDecimal credits = grade.getAssignment().getSubject().getCredits();
            weightedSum = weightedSum.add(BigDecimal.valueOf(grade.getScore()).multiply(credits));
            totalCredits = totalCredits.add(credits);
        }
        if (totalCredits.signum() == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return weightedSum.divide(totalCredits, 2, RoundingMode.HALF_UP);
    }
}
