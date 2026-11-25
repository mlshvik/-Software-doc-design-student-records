package ua.lpnu.studentrecords.pattern.strategy;

/**
 * Спосіб розрахунку середнього балу. Значення зберігається в налаштуванні {@code gpa.strategy}.
 */
public enum GpaStrategyType {
    /** Просте середнє арифметичне підсумкових оцінок. */
    SIMPLE,
    /** Середнє, зважене на кількість кредитів ЄКТС дисциплін. */
    CREDIT_WEIGHTED
}
