package ua.lpnu.studentrecords.service;

import ua.lpnu.studentrecords.pattern.strategy.GpaStrategyType;

import java.math.BigDecimal;

/**
 * Результат розрахунку середнього балу.
 *
 * @param value           середній бал за 100-бальною шкалою (з точністю до сотих)
 * @param ects            відповідна оцінка ЄКТС; {@code null}, якщо оцінок ще немає
 * @param strategy        спосіб розрахунку
 * @param subjectsCounted кількість дисциплін, урахованих у розрахунку
 * @param totalCredits    сума кредитів ЄКТС урахованих дисциплін
 */
public record GpaResult(BigDecimal value,
                        String ects,
                        GpaStrategyType strategy,
                        int subjectsCounted,
                        BigDecimal totalCredits) {
}
