package ua.lpnu.studentrecords.pattern.report;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Фабрика генераторів звітів.
 * <p>
 * <b>Шаблон Factory Method.</b> Клієнт ({@code ReportService}, контролер) знає лише тип звіту
 * {@link ReportType} і абстракцію {@link AbstractReportGenerator}; який конкретний клас створює
 * звіт, вирішує метод {@link #create(ReportType)}. Конкретні генератори – Spring-біни, які фабрика
 * отримує списком і реєструє за їхнім {@link AbstractReportGenerator#type()}. Додавання нового звіту
 * не змінює ні фабрику, ні клієнтів (принцип відкритості/закритості).
 */
@Component
public class ReportGeneratorFactory {

    private final Map<ReportType, AbstractReportGenerator<?>> generators = new EnumMap<>(ReportType.class);

    public ReportGeneratorFactory(List<AbstractReportGenerator<?>> generators) {
        for (AbstractReportGenerator<?> generator : generators) {
            AbstractReportGenerator<?> previous = this.generators.put(generator.type(), generator);
            if (previous != null) {
                throw new IllegalStateException("Для звіту " + generator.type() + " зареєстровано два генератори: "
                        + previous.getClass().getSimpleName() + " і " + generator.getClass().getSimpleName());
            }
        }
    }

    /**
     * Фабричний метод: повертає генератор для заданого типу звіту.
     *
     * @throws IllegalArgumentException якщо генератора для типу немає
     */
    public AbstractReportGenerator<?> create(ReportType type) {
        AbstractReportGenerator<?> generator = generators.get(type);
        if (generator == null) {
            throw new IllegalArgumentException("Звіт типу " + type + " не підтримується");
        }
        return generator;
    }

    public Set<ReportType> supportedTypes() {
        return Collections.unmodifiableSet(generators.keySet());
    }
}
