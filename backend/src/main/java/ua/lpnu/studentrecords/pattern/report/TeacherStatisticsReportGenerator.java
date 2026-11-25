package ua.lpnu.studentrecords.pattern.report;

import org.springframework.stereotype.Component;
import ua.lpnu.studentrecords.domain.entity.Grade;
import ua.lpnu.studentrecords.domain.entity.Teacher;
import ua.lpnu.studentrecords.domain.entity.TeachingAssignment;
import ua.lpnu.studentrecords.domain.enums.GradeAttempt;
import ua.lpnu.studentrecords.repository.GradeRepository;
import ua.lpnu.studentrecords.repository.TeachingAssignmentRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Звіт «Статистика оцінювання по викладачах» (конкретний клас шаблону Template Method).
 * <p>
 * Для кожного викладача: кількість дисциплін і груп, кількість підсумкових оцінок, середній бал
 * студентів, успішність і кількість перескладань. Викладачі без оцінок теж потрапляють у звіт.
 */
@Component
public class TeacherStatisticsReportGenerator
        extends AbstractReportGenerator<TeacherStatisticsReportGenerator.TeacherData> {

    /** Дані звіту: призначення викладачів і оцінки за ними. */
    public record TeacherData(List<TeachingAssignment> assignments, List<Grade> grades) {
    }

    private final TeachingAssignmentRepository assignmentRepository;
    private final GradeRepository gradeRepository;

    public TeacherStatisticsReportGenerator(TeachingAssignmentRepository assignmentRepository,
                                            GradeRepository gradeRepository) {
        this.assignmentRepository = assignmentRepository;
        this.gradeRepository = gradeRepository;
    }

    @Override
    public ReportType type() {
        return ReportType.TEACHER_STATISTICS;
    }

    @Override
    protected TeacherData loadData(ReportRequest request) {
        List<TeachingAssignment> assignments = assignmentRepository.findAllWithDetails().stream()
                .filter(a -> GroupPerformanceReportGenerator.matches(a, request))
                .toList();
        List<Grade> grades = gradeRepository.findAllForReports().stream()
                .filter(g -> GroupPerformanceReportGenerator.matches(g.getAssignment(), request))
                .toList();
        return new TeacherData(assignments, grades);
    }

    @Override
    protected List<ReportColumn> columns() {
        return List.of(
                ReportColumn.text("teacher", "Викладач"),
                ReportColumn.text("department", "Кафедра"),
                ReportColumn.number("subjects", "Дисциплін"),
                ReportColumn.number("groups", "Груп"),
                ReportColumn.number("grades", "Підсумкових оцінок"),
                ReportColumn.number("averageScore", "Середній бал"),
                ReportColumn.number("passRate", "Успішність, %"),
                ReportColumn.number("retakes", "Перескладань"));
    }

    @Override
    protected List<Map<String, Object>> aggregate(TeacherData data, ReportRequest request) {
        Map<Teacher, List<TeachingAssignment>> assignmentsByTeacher = new TreeMap<>(Comparator
                .comparing(Teacher::getLastName)
                .thenComparing(Teacher::getFirstName)
                .thenComparing(Teacher::getId));
        for (TeachingAssignment assignment : data.assignments()) {
            assignmentsByTeacher.computeIfAbsent(assignment.getTeacher(), t -> new ArrayList<>()).add(assignment);
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        assignmentsByTeacher.forEach((teacher, assignments) -> {
            List<Grade> grades = data.grades().stream()
                    .filter(g -> Objects.equals(g.getAssignment().getTeacher().getId(), teacher.getId()))
                    .toList();
            List<Grade> finals = ReportStatistics.finalGrades(grades);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("teacher", teacher.getShortName());
            row.put("department", teacher.getDepartment());
            row.put("subjects", assignments.stream().map(a -> a.getSubject().getId()).distinct().count());
            row.put("groups", assignments.stream().map(a -> a.getGroup().getId()).distinct().count());
            row.put("grades", finals.size());
            row.put("averageScore", ReportStatistics.average(finals));
            row.put("passRate", ReportStatistics.passRate(finals));
            row.put("retakes", grades.stream().filter(g -> g.getAttempt() != GradeAttempt.REGULAR).count());
            rows.add(row);
        });
        return rows;
    }

    @Override
    protected Map<String, Object> summarize(TeacherData data, List<Map<String, Object>> rows) {
        List<Grade> finals = ReportStatistics.finalGrades(data.grades());
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("teachers", rows.size());
        summary.put("assignments", data.assignments().size());
        summary.put("averageScore", ReportStatistics.average(finals));
        summary.put("passRate", ReportStatistics.passRate(finals));
        return summary;
    }
}
