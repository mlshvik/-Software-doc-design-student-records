package ua.lpnu.studentrecords.pattern.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ua.lpnu.studentrecords.service.GpaResult;
import ua.lpnu.studentrecords.service.GpaService;

/**
 * Перераховує середній бал студента після нової оцінки за допомогою {@link GpaService}
 * (стратегією, обраною в налаштуваннях) і фіксує результат у журналі.
 * <p>
 * <b>Шаблон Observer (роль ConcreteObserver).</b>
 */
@Component
public class GpaRecalculationObserver implements GradeEventListener {

    private static final Logger log = LoggerFactory.getLogger(GpaRecalculationObserver.class);

    private final GpaService gpaService;

    public GpaRecalculationObserver(GpaService gpaService) {
        this.gpaService = gpaService;
    }

    @Override
    public void onGradeRecorded(GradeRecordedEvent event) {
        Long studentId = event.student().getId();
        GpaResult result = gpaService.recalculate(studentId);
        log.info("Середній бал студента {} перераховано: {} ({})",
                studentId, result.value(), result.strategy());
    }
}
