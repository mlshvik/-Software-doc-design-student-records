package ua.lpnu.studentrecords.pattern.strategy;

import ua.lpnu.studentrecords.domain.entity.Grade;

import java.math.BigDecimal;
import java.util.List;

/**
 * Алгоритм розрахунку середнього балу студента.
 * <p>
 * <b>Шаблон Strategy (роль Strategy).</b> Заклад може рахувати середній бал по-різному
 * (просте середнє або зважене на кредити). Кожен алгоритм – окремий клас із спільним
 * інтерфейсом, тому {@code GpaService} (роль Context) не залежить від конкретної формули,
 * а нову формулу можна додати без зміни існуючого коду (принцип Open/Closed).
 */
public interface GpaCalculationStrategy {

    /** Тип стратегії, за яким її обирає {@link GpaCalculationStrategyResolver}. */
    GpaStrategyType type();

    /**
     * Розраховує середній бал за 100-бальною шкалою з точністю до сотих.
     * Для кожної дисципліни враховується лише підсумкова (найкраща) спроба.
     *
     * @param grades усі оцінки студента, включно з перескладаннями
     * @return середній бал; {@code 0.00}, якщо оцінок немає
     */
    BigDecimal calculate(List<Grade> grades);
}
