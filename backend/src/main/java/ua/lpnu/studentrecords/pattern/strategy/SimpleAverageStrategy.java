package ua.lpnu.studentrecords.pattern.strategy;

import org.springframework.stereotype.Component;
import ua.lpnu.studentrecords.domain.entity.Grade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Просте середнє арифметичне підсумкових оцінок: {@code Σ бал / кількість дисциплін}.
 * <p>
 * <b>Шаблон Strategy (роль ConcreteStrategy).</b>
 */
@Component
public class SimpleAverageStrategy implements GpaCalculationStrategy {

    @Override
    public GpaStrategyType type() {
        return GpaStrategyType.SIMPLE;
    }

    @Override
    public BigDecimal calculate(List<Grade> grades) {
        List<Grade> finals = FinalGradeSelector.selectFinal(grades);
        if (finals.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (Grade grade : finals) {
            sum = sum.add(BigDecimal.valueOf(grade.getScore()));
        }
        return sum.divide(BigDecimal.valueOf(finals.size()), 2, RoundingMode.HALF_UP);
    }
}
