package ua.lpnu.studentrecords;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ua.lpnu.studentrecords.pattern.chain.GradeValidationChainBuilder;
import ua.lpnu.studentrecords.pattern.facade.StudentRecordFacade;
import ua.lpnu.studentrecords.pattern.observer.AcademicDebtObserver;
import ua.lpnu.studentrecords.pattern.observer.GpaRecalculationObserver;
import ua.lpnu.studentrecords.pattern.observer.GradeEventListener;
import ua.lpnu.studentrecords.pattern.observer.GradeEventPublisher;
import ua.lpnu.studentrecords.pattern.observer.NotificationObserver;
import ua.lpnu.studentrecords.pattern.strategy.CreditWeightedStrategy;
import ua.lpnu.studentrecords.pattern.strategy.GpaCalculationStrategyResolver;
import ua.lpnu.studentrecords.pattern.strategy.GpaStrategyType;
import ua.lpnu.studentrecords.pattern.strategy.SimpleAverageStrategy;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Перевіряє, що контекст Spring піднімається (профіль {@code test}, H2) і біни шаблонів
 * пов'язані між собою: спостерігачі підписані на видавця, стратегії зареєстровані.
 */
@SpringBootTest
@ActiveProfiles("test")
class ApplicationContextTest {

    @Autowired
    private GradeEventPublisher publisher;

    @Autowired
    private GpaCalculationStrategyResolver strategyResolver;

    @Autowired
    private GradeValidationChainBuilder chainBuilder;

    @Autowired
    private StudentRecordFacade facade;

    @Test
    void observersAreSubscribedAutomatically() {
        List<GradeEventListener> listeners = publisher.getListeners();
        assertEquals(3, listeners.size());
        assertTrue(listeners.stream().anyMatch(AcademicDebtObserver.class::isInstance));
        assertTrue(listeners.stream().anyMatch(NotificationObserver.class::isInstance));
        assertTrue(listeners.stream().anyMatch(GpaRecalculationObserver.class::isInstance));
    }

    @Test
    void allGpaStrategiesAreRegistered() {
        assertInstanceOf(SimpleAverageStrategy.class, strategyResolver.resolve(GpaStrategyType.SIMPLE));
        assertInstanceOf(CreditWeightedStrategy.class, strategyResolver.resolve(GpaStrategyType.CREDIT_WEIGHTED));
        // у порожній БД налаштування немає – використовується стратегія за замовчуванням
        assertInstanceOf(CreditWeightedStrategy.class, strategyResolver.resolve());
    }

    @Test
    void facadeAndChainAreAvailable() {
        assertNotNull(facade);
        assertNotNull(chainBuilder.build());
    }
}
