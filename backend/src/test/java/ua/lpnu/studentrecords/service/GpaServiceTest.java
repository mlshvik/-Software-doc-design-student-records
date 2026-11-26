package ua.lpnu.studentrecords.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.entity.Teacher;
import ua.lpnu.studentrecords.domain.entity.TeachingAssignment;
import ua.lpnu.studentrecords.domain.enums.ControlType;
import ua.lpnu.studentrecords.domain.enums.GradeAttempt;
import ua.lpnu.studentrecords.pattern.strategy.CreditWeightedStrategy;
import ua.lpnu.studentrecords.pattern.strategy.GpaCalculationStrategyResolver;
import ua.lpnu.studentrecords.pattern.strategy.GpaStrategyType;
import ua.lpnu.studentrecords.pattern.strategy.SimpleAverageStrategy;
import ua.lpnu.studentrecords.repository.GradeRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ua.lpnu.studentrecords.support.TestData.assignment;
import static ua.lpnu.studentrecords.support.TestData.grade;
import static ua.lpnu.studentrecords.support.TestData.group;
import static ua.lpnu.studentrecords.support.TestData.student;
import static ua.lpnu.studentrecords.support.TestData.subject;
import static ua.lpnu.studentrecords.support.TestData.teacher;

/**
 * Тести сервісу середнього балу (Context шаблону Strategy).
 */
class GpaServiceTest {

    private GradeRepository gradeRepository;
    private GpaCalculationStrategyResolver resolver;
    private GpaService service;

    @BeforeEach
    void setUp() {
        gradeRepository = mock(GradeRepository.class);
        resolver = mock(GpaCalculationStrategyResolver.class);
        service = new GpaService(gradeRepository, resolver);

        Student student = student(1L, group(1L, "ІР-43"));
        Teacher teacher = teacher(1L, "Коваль");
        TeachingAssignment a1 = assignment(1L, subject(1L, "Бази даних", "6.0", ControlType.EXAM), teacher, student.getGroup());
        TeachingAssignment a2 = assignment(2L, subject(2L, "Філософія", "2.0", ControlType.CREDIT), teacher, student.getGroup());
        when(gradeRepository.findByStudentIdOrderByGradedAtAsc(1L)).thenReturn(List.of(
                grade(student, a1, 92, GradeAttempt.REGULAR),
                grade(student, a2, 50, GradeAttempt.REGULAR),
                grade(student, a2, 64, GradeAttempt.RETAKE_TEACHER)));
    }

    @Test
    void usesStrategyChosenInSettings() {
        when(resolver.resolve()).thenReturn(new CreditWeightedStrategy());

        GpaResult result = service.getGpa(1L);

        // (92·6 + 64·2) / 8 = 85.00
        assertEquals(new BigDecimal("85.00"), result.value());
        assertEquals("B", result.ects());
        assertEquals(GpaStrategyType.CREDIT_WEIGHTED, result.strategy());
        assertEquals(2, result.subjectsCounted());
        assertEquals(0, new BigDecimal("8").compareTo(result.totalCredits()));
    }

    @Test
    void switchingStrategyChangesResult() {
        when(resolver.resolve()).thenReturn(new SimpleAverageStrategy());

        // (92 + 64) / 2 = 78.00
        assertEquals(new BigDecimal("78.00"), service.calculateGpa(1L));
    }

    @Test
    void studentWithoutGradesHasZeroGpa() {
        when(resolver.resolve()).thenReturn(new SimpleAverageStrategy());
        when(gradeRepository.findByStudentIdOrderByGradedAtAsc(2L)).thenReturn(List.of());

        GpaResult result = service.getGpa(2L);

        assertEquals(new BigDecimal("0.00"), result.value());
        assertNull(result.ects());
        assertEquals(0, result.subjectsCounted());
    }
}
