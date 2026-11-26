package ua.lpnu.studentrecords.pattern.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.lpnu.studentrecords.domain.entity.AcademicDebt;
import ua.lpnu.studentrecords.domain.entity.Grade;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.entity.StudyGroup;
import ua.lpnu.studentrecords.domain.entity.Subject;
import ua.lpnu.studentrecords.domain.entity.Teacher;
import ua.lpnu.studentrecords.domain.entity.TeachingAssignment;
import ua.lpnu.studentrecords.domain.enums.ControlType;
import ua.lpnu.studentrecords.domain.enums.GradeAttempt;
import ua.lpnu.studentrecords.repository.AcademicDebtRepository;
import ua.lpnu.studentrecords.repository.GradeRepository;
import ua.lpnu.studentrecords.repository.TeachingAssignmentRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
 * Тести шаблону Template Method: незмінний порядок кроків і три конкретні звіти.
 * <p>
 * Дані: група ІР-43 (4 курс) – студент 1 має 90 з «Баз даних» і 40 → 75 після перескладання
 * з «Архітектури ПЗ», студент 2 – 55 з «Баз даних»; група ІР-21 (2 курс) – студент 3 має 82.
 */
class ReportGeneratorsTest {

    private GradeRepository gradeRepository;
    private TeachingAssignmentRepository assignmentRepository;
    private AcademicDebtRepository debtRepository;

    private TeachingAssignment databases;
    private TeachingAssignment architecture;
    private TeachingAssignment secondYearDatabases;
    private TeachingAssignment unusedAssignment;
    private Student first;
    private Student second;
    private final List<Grade> grades = new ArrayList<>();

    @BeforeEach
    void setUp() {
        gradeRepository = mock(GradeRepository.class);
        assignmentRepository = mock(TeachingAssignmentRepository.class);
        debtRepository = mock(AcademicDebtRepository.class);

        StudyGroup ir43 = group(1L, "ІР-43");
        StudyGroup ir21 = group(2L, "ІР-21");
        ir21.setCourse(2);
        Teacher kovalenko = teacher(1L, "Коваленко");
        Teacher bondar = teacher(2L, "Бондар");
        Teacher yarema = teacher(3L, "Ярема");
        Subject db = subject(1L, "Бази даних", "5.0", ControlType.EXAM);
        Subject arch = subject(2L, "Архітектура ПЗ", "4.0", ControlType.EXAM);

        databases = assignment(1L, db, kovalenko, ir43);
        architecture = assignment(2L, arch, bondar, ir43);
        secondYearDatabases = assignment(3L, db, kovalenko, ir21);
        unusedAssignment = assignment(4L, arch, yarema, ir21);

        first = student(1L, ir43);
        second = student(2L, ir43);
        Student third = student(3L, ir21);

        grades.add(grade(first, databases, 90, GradeAttempt.REGULAR));
        grades.add(grade(first, architecture, 40, GradeAttempt.REGULAR));
        grades.add(grade(first, architecture, 75, GradeAttempt.RETAKE_TEACHER));
        grades.add(grade(second, databases, 55, GradeAttempt.REGULAR));
        grades.add(grade(third, secondYearDatabases, 82, GradeAttempt.REGULAR));
        when(gradeRepository.findAllForReports()).thenReturn(grades);
        when(assignmentRepository.findAllWithDetails())
                .thenReturn(List.of(databases, architecture, secondYearDatabases, unusedAssignment));
    }

    @Test
    void templateMethodRunsStepsInFixedOrder() {
        List<String> calls = new ArrayList<>();
        AbstractReportGenerator<String> generator = new AbstractReportGenerator<>() {
            @Override
            public ReportType type() {
                return ReportType.GROUP_PERFORMANCE;
            }

            @Override
            protected String loadData(ReportRequest request) {
                calls.add("loadData");
                return "дані";
            }

            @Override
            protected List<Map<String, Object>> aggregate(String data, ReportRequest request) {
                calls.add("aggregate");
                return List.of(Map.of("value", data));
            }

            @Override
            protected List<ReportColumn> columns() {
                return List.of(ReportColumn.text("value", "Значення"));
            }

            @Override
            protected Map<String, Object> summarize(String data, List<Map<String, Object>> rows) {
                calls.add("summarize");
                return Map.of("rows", rows.size());
            }
        };

        Report report = generator.generate(null);

        assertEquals(List.of("loadData", "aggregate", "summarize"), calls);
        assertEquals("Успішність за групами та курсами", report.getTitle());
        assertEquals("дані", report.getRows().get(0).get("value"));
        assertEquals(1, report.getSummary().get("rows"));
    }

    @Test
    void groupPerformanceCountsOnlyFinalGrades() {
        Report report = new GroupPerformanceReportGenerator(gradeRepository).generate(ReportRequest.empty());

        assertEquals(2, report.getRowCount());
        Map<String, Object> ir21 = report.getRows().get(0);
        Map<String, Object> ir43 = report.getRows().get(1);
        assertEquals("ІР-21", ir21.get("group"), "групи впорядковано за курсом");
        assertEquals(new BigDecimal("82.00"), ir21.get("averageScore"));

        assertEquals("ІР-43", ir43.get("group"));
        assertEquals(2L, ir43.get("students"));
        assertEquals(3, ir43.get("grades"), "незадовільна спроба 40 замінена перескладанням 75");
        assertEquals(new BigDecimal("73.33"), ir43.get("averageScore"));
        assertEquals(new BigDecimal("66.7"), ir43.get("passRate"));
        assertEquals(new BigDecimal("66.7"), ir43.get("qualityRate"));
        assertEquals(1L, ir43.get("failed"));

        assertEquals(new BigDecimal("75.50"), report.getSummary().get("averageScore"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> byCourse = (List<Map<String, Object>>) report.getSummary().get("byCourse");
        assertEquals(List.of(2, 4), byCourse.stream().map(c -> c.get("course")).toList());
    }

    @Test
    void groupPerformanceAppliesFilters() {
        Report report = new GroupPerformanceReportGenerator(gradeRepository)
                .generate(new ReportRequest("2026/2027", 7, 1L, 4));

        assertEquals(1, report.getRowCount());
        assertEquals("ІР-43", report.getRows().get(0).get("group"));
        assertEquals("Успішність за групами та курсами (2026/2027)", report.getTitle());
        assertEquals(4, report.getParameters().get("course"));

        Report otherYear = new GroupPerformanceReportGenerator(gradeRepository)
                .generate(new ReportRequest("2020/2021", null, null, null));
        assertEquals(0, otherYear.getRowCount());
    }

    @Test
    void teacherStatisticsIncludesTeachersWithoutGrades() {
        Report report = new TeacherStatisticsReportGenerator(assignmentRepository, gradeRepository)
                .generate(ReportRequest.empty());

        assertEquals(List.of("Бондар О. І.", "Коваленко О. І.", "Ярема О. І."),
                report.getRows().stream().map(r -> r.get("teacher")).toList());

        Map<String, Object> bondar = report.getRows().get(0);
        assertEquals(new BigDecimal("75.00"), bondar.get("averageScore"));
        assertEquals(1L, bondar.get("retakes"));

        Map<String, Object> kovalenko = report.getRows().get(1);
        assertEquals(1L, kovalenko.get("subjects"));
        assertEquals(2L, kovalenko.get("groups"));
        assertEquals(3, kovalenko.get("grades"));
        assertEquals(new BigDecimal("75.67"), kovalenko.get("averageScore"));
        assertEquals(new BigDecimal("66.7"), kovalenko.get("passRate"));

        Map<String, Object> yarema = report.getRows().get(2);
        assertEquals(0, yarema.get("grades"));
        assertNull(yarema.get("averageScore"));
        assertEquals(3, report.getSummary().get("teachers"));
    }

    @Test
    void academicDebtReportListsOpenDebts() {
        AcademicDebt open = new AcademicDebt(second, databases);
        open.setOpenedAt(LocalDateTime.now().minusDays(10));
        AcademicDebt closed = new AcademicDebt(first, architecture);
        closed.close();
        when(debtRepository.findAllForReports()).thenReturn(List.of(open, closed));

        Report report = new AcademicDebtReportGenerator(debtRepository).generate(ReportRequest.empty());

        assertEquals(1, report.getRowCount());
        Map<String, Object> row = report.getRows().get(0);
        assertEquals("Бази даних", row.get("subject"));
        assertEquals("ІР-43", row.get("group"));
        assertEquals(10L, row.get("daysOpen"));
        assertEquals(1, report.getSummary().get("open"));
        assertEquals(1L, report.getSummary().get("closed"));
        assertEquals(1L, report.getSummary().get("studentsWithDebts"));
    }
}
