package ua.lpnu.studentrecords.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lpnu.studentrecords.domain.entity.SystemSetting;
import ua.lpnu.studentrecords.domain.exception.NotFoundException;
import ua.lpnu.studentrecords.pattern.strategy.GpaCalculationStrategyResolver;
import ua.lpnu.studentrecords.pattern.strategy.GpaStrategyType;
import ua.lpnu.studentrecords.repository.SystemSettingRepository;
import ua.lpnu.studentrecords.web.dto.SettingResponse;
import ua.lpnu.studentrecords.web.mapper.AcademicMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Системні налаштування. Зараз підтримується {@code gpa.strategy} – стратегія розрахунку
 * середнього балу (шаблон Strategy): зміна значення одразу впливає на всі розрахунки,
 * бо {@link GpaCalculationStrategyResolver} читає його під час кожного обчислення.
 */
@Service
@Transactional(readOnly = true)
public class SettingService {

    /** Відомі налаштування та їхні описи. */
    private static final Map<String, String> KNOWN_SETTINGS = Map.of(
            SystemSetting.GPA_STRATEGY,
            "Стратегія розрахунку середнього балу: SIMPLE – просте середнє, CREDIT_WEIGHTED – зважене за кредитами");

    private final SystemSettingRepository settingRepository;

    public SettingService(SystemSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    /** Усі налаштування; відомі налаштування, яких ще немає в БД, показуються зі значенням за замовчуванням. */
    public List<SettingResponse> findAll() {
        List<SettingResponse> result = new ArrayList<>(
                settingRepository.findAll().stream().map(AcademicMapper::toResponse).toList());
        KNOWN_SETTINGS.forEach((key, description) -> {
            if (result.stream().noneMatch(s -> s.key().equals(key))) {
                result.add(new SettingResponse(key, defaultValue(key), description));
            }
        });
        result.sort(Comparator.comparing(SettingResponse::key));
        return result;
    }

    /**
     * Змінює значення налаштування.
     *
     * @throws IllegalArgumentException якщо значення недопустиме (напр., невідома стратегія)
     * @throws NotFoundException        якщо налаштування невідоме
     */
    @Transactional
    public SettingResponse update(String key, String value) {
        String normalized = validate(key, value.trim());
        SystemSetting setting = settingRepository.findById(key).orElseGet(() -> {
            if (!KNOWN_SETTINGS.containsKey(key)) {
                throw new NotFoundException("Налаштування", key);
            }
            return new SystemSetting(key, normalized, KNOWN_SETTINGS.get(key));
        });
        setting.setValue(normalized);
        return AcademicMapper.toResponse(settingRepository.save(setting));
    }

    private static String validate(String key, String value) {
        if (SystemSetting.GPA_STRATEGY.equals(key)) {
            String upper = value.toUpperCase(Locale.ROOT);
            boolean known = Arrays.stream(GpaStrategyType.values()).anyMatch(t -> t.name().equals(upper));
            if (!known) {
                throw new IllegalArgumentException("Невідома стратегія середнього балу: " + value
                        + ". Допустимі значення: " + Arrays.toString(GpaStrategyType.values()));
            }
            return upper;
        }
        return value;
    }

    private static String defaultValue(String key) {
        return SystemSetting.GPA_STRATEGY.equals(key) ? GpaCalculationStrategyResolver.DEFAULT_TYPE.name() : "";
    }
}
