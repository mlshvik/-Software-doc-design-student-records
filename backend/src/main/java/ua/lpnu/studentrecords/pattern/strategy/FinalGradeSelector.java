package ua.lpnu.studentrecords.pattern.strategy;

import ua.lpnu.studentrecords.domain.entity.Grade;
import ua.lpnu.studentrecords.domain.entity.TeachingAssignment;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Відбір підсумкових оцінок: для кожної дисципліни (призначення викладача групі)
 * залишається одна оцінка – з найбільшим балом, а за рівності балів – найпізніша спроба.
 */
public final class FinalGradeSelector {

    private static final Comparator<Grade> BEST_FIRST = Comparator
            .comparing(Grade::getScore)
            .thenComparing(Grade::getAttempt);

    private FinalGradeSelector() {
    }

    public static List<Grade> selectFinal(List<Grade> grades) {
        Map<Object, Grade> best = new LinkedHashMap<>();
        for (Grade grade : grades) {
            if (grade == null || grade.getScore() == null || grade.getAssignment() == null) {
                continue;
            }
            best.merge(keyOf(grade.getAssignment()), grade,
                    (current, candidate) -> BEST_FIRST.compare(candidate, current) > 0 ? candidate : current);
        }
        return new ArrayList<>(best.values());
    }

    /** Ключ групування: ідентифікатор призначення, а для ще не збереженого – сам об'єкт. */
    private static Object keyOf(TeachingAssignment assignment) {
        return assignment.getId() != null ? assignment.getId() : assignment;
    }
}
