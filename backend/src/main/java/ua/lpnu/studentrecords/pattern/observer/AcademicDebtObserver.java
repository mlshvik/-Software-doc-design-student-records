package ua.lpnu.studentrecords.pattern.observer;

import org.springframework.stereotype.Component;
import ua.lpnu.studentrecords.service.DebtService;

/**
 * Веде академічні заборгованості: незадовільна оцінка відкриває борг (якщо його ще немає),
 * позитивна – закриває відкритий борг з цієї дисципліни.
 * <p>
 * <b>Шаблон Observer (роль ConcreteObserver).</b>
 */
@Component
public class AcademicDebtObserver implements GradeEventListener {

    private final DebtService debtService;

    public AcademicDebtObserver(DebtService debtService) {
        this.debtService = debtService;
    }

    @Override
    public void onGradeRecorded(GradeRecordedEvent event) {
        if (event.passed()) {
            debtService.closeDebt(event.student(), event.assignment());
        } else {
            debtService.openDebt(event.student(), event.assignment());
        }
    }
}
