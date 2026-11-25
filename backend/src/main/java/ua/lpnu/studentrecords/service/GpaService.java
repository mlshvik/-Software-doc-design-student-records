package ua.lpnu.studentrecords.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lpnu.studentrecords.domain.entity.Grade;
import ua.lpnu.studentrecords.pattern.scale.GradingScale;
import ua.lpnu.studentrecords.pattern.strategy.FinalGradeSelector;
import ua.lpnu.studentrecords.pattern.strategy.GpaCalculationStrategy;
import ua.lpnu.studentrecords.pattern.strategy.GpaCalculationStrategyResolver;
import ua.lpnu.studentrecords.repository.GradeRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Розрахунок середнього балу студента.
 * <p>
 * <b>Шаблон Strategy (роль Context).</b> Сервіс не містить формули розрахунку – він отримує
 * стратегію від {@link GpaCalculationStrategyResolver} за налаштуванням {@code gpa.strategy}
 * і делегує їй обчислення.
 */
@Service
@Transactional(readOnly = true)
public class GpaService {

    private final GradeRepository gradeRepository;
    private final GpaCalculationStrategyResolver strategyResolver;

    public GpaService(GradeRepository gradeRepository, GpaCalculationStrategyResolver strategyResolver) {
        this.gradeRepository = gradeRepository;
        this.strategyResolver = strategyResolver;
    }

    /** Середній бал студента (лише значення). */
    public BigDecimal calculateGpa(Long studentId) {
        return getGpa(studentId).value();
    }

    /** Середній бал студента з деталями розрахунку. */
    public GpaResult getGpa(Long studentId) {
        return calculate(gradeRepository.findByStudentIdOrderByGradedAtAsc(studentId));
    }

    /** Повторний розрахунок після зміни оцінок (викликається спостерігачем). */
    public GpaResult recalculate(Long studentId) {
        return getGpa(studentId);
    }

    /** Розрахунок для готового списку оцінок поточною стратегією. */
    public GpaResult calculate(List<Grade> grades) {
        return calculate(grades, strategyResolver.resolve());
    }

    /** Розрахунок для готового списку оцінок заданою стратегією. */
    public GpaResult calculate(List<Grade> grades, GpaCalculationStrategy strategy) {
        List<Grade> finals = FinalGradeSelector.selectFinal(grades);
        BigDecimal value = strategy.calculate(grades);
        BigDecimal credits = BigDecimal.ZERO;
        for (Grade grade : finals) {
            credits = credits.add(grade.getAssignment().getSubject().getCredits());
        }
        String ects = finals.isEmpty()
                ? null
                : GradingScale.INSTANCE.toEcts(value.setScale(0, RoundingMode.HALF_UP).intValue());
        return new GpaResult(value, ects, strategy.type(), finals.size(), credits);
    }
}
