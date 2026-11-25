package ua.lpnu.studentrecords.pattern.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.lpnu.studentrecords.domain.entity.SystemSetting;
import ua.lpnu.studentrecords.repository.SystemSettingRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Тести вибору стратегії за налаштуванням {@code gpa.strategy}.
 */
class GpaCalculationStrategyResolverTest {

    private SystemSettingRepository settings;
    private GpaCalculationStrategyResolver resolver;

    @BeforeEach
    void setUp() {
        settings = mock(SystemSettingRepository.class);
        resolver = new GpaCalculationStrategyResolver(
                List.of(new SimpleAverageStrategy(), new CreditWeightedStrategy()), settings);
    }

    private void givenSetting(String value) {
        when(settings.findById(SystemSetting.GPA_STRATEGY))
                .thenReturn(Optional.of(new SystemSetting(SystemSetting.GPA_STRATEGY, value, null)));
    }

    @Test
    void resolvesSimpleStrategyFromSetting() {
        givenSetting("SIMPLE");
        assertInstanceOf(SimpleAverageStrategy.class, resolver.resolve());
    }

    @Test
    void resolvesCreditWeightedStrategyIgnoringCase() {
        givenSetting(" credit_weighted ");
        assertInstanceOf(CreditWeightedStrategy.class, resolver.resolve());
    }

    @Test
    void fallsBackToDefaultForUnknownValue() {
        givenSetting("MEDIAN");
        assertEquals(GpaCalculationStrategyResolver.DEFAULT_TYPE, resolver.currentType());
    }

    @Test
    void fallsBackToDefaultWhenSettingIsMissing() {
        when(settings.findById(SystemSetting.GPA_STRATEGY)).thenReturn(Optional.empty());
        assertInstanceOf(CreditWeightedStrategy.class, resolver.resolve());
    }
}
