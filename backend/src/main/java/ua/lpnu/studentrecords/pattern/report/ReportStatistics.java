package ua.lpnu.studentrecords.pattern.report;

import ua.lpnu.studentrecords.domain.entity.Grade;
import ua.lpnu.studentrecords.pattern.strategy.FinalGradeSelector;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Допоміжні обчислення для звітів: підсумкові оцінки, середній бал, відсоток успішності.
 */
public final class ReportStatistics {

    private ReportStatistics() {
    }

    /**
     * Підсумкові оцінки: для кожної пари «студент – дисципліна групи» лише найкраща спроба
     * (та сама логіка, що й у розрахунку середнього балу).
     */
    public static List<Grade> finalGrades(Collection<Grade> grades) {
        Map<Long, List<Grade>> byStudent = new LinkedHashMap<>();
        for (Grade grade : grades) {
            byStudent.computeIfAbsent(grade.getStudent().getId(), id -> new ArrayList<>()).add(grade);
        }
        List<Grade> result = new ArrayList<>();
        byStudent.values().forEach(list -> result.addAll(FinalGradeSelector.selectFinal(list)));
        return result;
    }

    /** Середній бал з двома знаками після коми або {@code null}, якщо оцінок немає. */
    public static BigDecimal average(Collection<Grade> grades) {
        if (grades.isEmpty()) {
            return null;
        }
        long sum = 0;
        for (Grade grade : grades) {
            sum += grade.getScore();
        }
        return BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(grades.size()), 2, RoundingMode.HALF_UP);
    }

    /** Відсоток позитивних оцінок (≥ 60) з одним знаком після коми або {@code null}. */
    public static BigDecimal passRate(Collection<Grade> grades) {
        if (grades.isEmpty()) {
            return null;
        }
        long passed = grades.stream().filter(Grade::isPassed).count();
        return percent(passed, grades.size());
    }

    public static BigDecimal percent(long part, long total) {
        if (total == 0) {
            return null;
        }
        return BigDecimal.valueOf(part * 100L).divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
    }
}
