package ua.lpnu.studentrecords.pattern.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ua.lpnu.studentrecords.domain.entity.SystemSetting;
import ua.lpnu.studentrecords.repository.SystemSettingRepository;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Обирає стратегію розрахунку середнього балу за налаштуванням системи {@code gpa.strategy}.
 * <p>
 * Усі реалізації {@link GpaCalculationStrategy} – Spring-біни, які впроваджуються списком
 * і реєструються за своїм типом. Адміністратор змінює спосіб розрахунку в налаштуваннях
 * без перезапуску застосунку. Невідоме значення налаштування – {@link #DEFAULT_TYPE}.
 */
@Component
public class GpaCalculationStrategyResolver {

    public static final GpaStrategyType DEFAULT_TYPE = GpaStrategyType.CREDIT_WEIGHTED;

    private static final Logger log = LoggerFactory.getLogger(GpaCalculationStrategyResolver.class);

    private final Map<GpaStrategyType, GpaCalculationStrategy> strategies = new EnumMap<>(GpaStrategyType.class);
    private final SystemSettingRepository settingRepository;

    public GpaCalculationStrategyResolver(List<GpaCalculationStrategy> strategies,
                                          SystemSettingRepository settingRepository) {
        for (GpaCalculationStrategy strategy : strategies) {
            this.strategies.put(strategy.type(), strategy);
        }
        this.settingRepository = settingRepository;
    }

    /** Стратегія, задана поточним налаштуванням системи. */
    public GpaCalculationStrategy resolve() {
        return resolve(currentType());
    }

    /** Стратегія заданого типу. */
    public GpaCalculationStrategy resolve(GpaStrategyType type) {
        GpaCalculationStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalStateException("Стратегію розрахунку середнього балу не зареєстровано: " + type);
        }
        return strategy;
    }

    /** Тип стратегії з налаштування {@code gpa.strategy}. */
    public GpaStrategyType currentType() {
        String value = settingRepository.findById(SystemSetting.GPA_STRATEGY)
                .map(SystemSetting::getValue)
                .orElse(DEFAULT_TYPE.name());
        try {
            return GpaStrategyType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Невідоме значення налаштування {}='{}', використовується {}",
                    SystemSetting.GPA_STRATEGY, value, DEFAULT_TYPE);
            return DEFAULT_TYPE;
        }
    }
}
