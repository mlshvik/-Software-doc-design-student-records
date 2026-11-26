package ua.lpnu.studentrecords.pattern.report;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ua.lpnu.studentrecords.repository.AcademicDebtRepository;
import ua.lpnu.studentrecords.repository.GradeRepository;
import ua.lpnu.studentrecords.repository.TeachingAssignmentRepository;

import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**
 * Тести шаблону Factory Method: фабрика повертає генератор за типом звіту.
 */
class ReportGeneratorFactoryTest {

    private final GradeRepository gradeRepository = mock(GradeRepository.class);
    private final GroupPerformanceReportGenerator groupPerformance = new GroupPerformanceReportGenerator(gradeRepository);
    private final TeacherStatisticsReportGenerator teacherStatistics =
            new TeacherStatisticsReportGenerator(mock(TeachingAssignmentRepository.class), gradeRepository);
    private final AcademicDebtReportGenerator academicDebts =
            new AcademicDebtReportGenerator(mock(AcademicDebtRepository.class));

    @Test
    void createsGeneratorForEachType() {
        ReportGeneratorFactory factory =
                new ReportGeneratorFactory(List.of(groupPerformance, teacherStatistics, academicDebts));

        assertInstanceOf(GroupPerformanceReportGenerator.class, factory.create(ReportType.GROUP_PERFORMANCE));
        assertInstanceOf(TeacherStatisticsReportGenerator.class, factory.create(ReportType.TEACHER_STATISTICS));
        assertInstanceOf(AcademicDebtReportGenerator.class, factory.create(ReportType.ACADEMIC_DEBTS));
        assertEquals(EnumSet.allOf(ReportType.class), factory.supportedTypes());
    }

    @Test
    void unsupportedTypeIsRejected() {
        ReportGeneratorFactory factory = new ReportGeneratorFactory(List.of(groupPerformance));

        assertThrows(IllegalArgumentException.class, () -> factory.create(ReportType.ACADEMIC_DEBTS));
    }

    @Test
    void duplicateGeneratorsAreRejected() {
        GroupPerformanceReportGenerator duplicate = new GroupPerformanceReportGenerator(gradeRepository);

        assertThrows(IllegalStateException.class,
                () -> new ReportGeneratorFactory(List.of(groupPerformance, duplicate)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"group-performance", "GROUP_PERFORMANCE", "Group-Performance", " group_performance "})
    void reportTypeIsParsedFromUrl(String value) {
        assertEquals(ReportType.GROUP_PERFORMANCE, ReportType.fromValue(value));
    }

    @Test
    void unknownReportTypeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> ReportType.fromValue("salary"));
        assertThrows(IllegalArgumentException.class, () -> ReportType.fromValue(null));
    }
}
