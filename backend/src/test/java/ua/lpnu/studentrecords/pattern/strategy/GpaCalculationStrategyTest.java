package ua.lpnu.studentrecords.pattern.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.lpnu.studentrecords.domain.entity.Grade;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.entity.StudyGroup;
import ua.lpnu.studentrecords.domain.entity.Teacher;
import ua.lpnu.studentrecords.domain.entity.TeachingAssignment;
import ua.lpnu.studentrecords.domain.enums.ControlType;
import ua.lpnu.studentrecords.domain.enums.GradeAttempt;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static ua.lpnu.studentrecords.support.TestData.assignment;
import static ua.lpnu.studentrecords.support.TestData.grade;
import static ua.lpnu.studentrecords.support.TestData.group;
import static ua.lpnu.studentrecords.support.TestData.student;
import static ua.lpnu.studentrecords.support.TestData.subject;
import static ua.lpnu.studentrecords.support.TestData.teacher;

/**
 * Тести шаблону Strategy – двох алгоритмів розрахунку середнього балу.
 */
class GpaCalculationStrategyTest {

    private List<Grade> grades;
    private TeachingAssignment retakenAssignment;
    private Student student;

    @BeforeEach
    void setUp() {
        StudyGroup group = group(1L, "ІР-43");
        Teacher teacher = teacher(1L, "Коваль");
        student = student(1L, group);
        TeachingAssignment databases = assignment(1L, subject(1L, "Бази даних", "6.0", ControlType.EXAM), teacher, group);
        retakenAssignment = assignment(2L, subject(2L, "Філософія", "3.0", ControlType.CREDIT), teacher, group);
        TeachingAssignment patterns = assignment(3L, subject(3L, "Шаблони проектування", "3.0", ControlType.EXAM), teacher, group);
        grades = List.of(
                grade(student, databases, 90, GradeAttempt.REGULAR),
                grade(student, retakenAssignment, 40, GradeAttempt.REGULAR),
                grade(student, retakenAssignment, 70, GradeAttempt.RETAKE_TEACHER),
                grade(student, patterns, 81, GradeAttempt.REGULAR));
    }

    @Test
    void simpleAverageUsesFinalAttemptOfEachSubject() {
        // (90 + 70 + 81) / 3 = 80.333…
        assertEquals(new BigDecimal("80.33"), new SimpleAverageStrategy().calculate(grades));
    }

    @Test
    void creditWeightedAverageTakesCreditsIntoAccount() {
        // (90·6 + 70·3 + 81·3) / 12 = 82.75
        assertEquals(new BigDecimal("82.75"), new CreditWeightedStrategy().calculate(grades));
    }

    @Test
    void emptyGradesGiveZero() {
        assertEquals(new BigDecimal("0.00"), new SimpleAverageStrategy().calculate(List.of()));
        assertEquals(new BigDecimal("0.00"), new CreditWeightedStrategy().calculate(List.of()));
    }

    @Test
    void finalGradeSelectorKeepsBestAttemptPerSubject() {
        List<Grade> finals = FinalGradeSelector.selectFinal(grades);
        assertEquals(3, finals.size());
        Grade philosophy = finals.stream()
                .filter(g -> g.getAssignment() == retakenAssignment)
                .findFirst().orElseThrow();
        assertEquals(70, philosophy.getScore());
        assertEquals(GradeAttempt.RETAKE_TEACHER, philosophy.getAttempt());
    }

    @Test
    void strategiesReportTheirType() {
        assertSame(GpaStrategyType.SIMPLE, new SimpleAverageStrategy().type());
        assertSame(GpaStrategyType.CREDIT_WEIGHTED, new CreditWeightedStrategy().type());
    }
}
