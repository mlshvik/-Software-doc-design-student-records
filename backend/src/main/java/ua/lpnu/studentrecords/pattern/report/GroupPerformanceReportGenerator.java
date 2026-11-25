package ua.lpnu.studentrecords.pattern.report;

import org.springframework.stereotype.Component;
import ua.lpnu.studentrecords.domain.entity.Grade;
import ua.lpnu.studentrecords.domain.entity.StudyGroup;
import ua.lpnu.studentrecords.domain.entity.TeachingAssignment;
import ua.lpnu.studentrecords.repository.GradeRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Звіт «Успішність за групами та курсами» (конкретний клас шаблону Template Method).
 * <p>
 * Для кожної групи: кількість студентів з оцінками, середній бал, успішність (частка оцінок ≥ 60)
 * та якість знань (частка оцінок ≥ 74, тобто «добре» і «відмінно»). У підсумках – ті самі показники
 * за кожним курсом і загалом. Враховуються лише підсумкові оцінки (найкраща спроба).
 */
@Component
public class GroupPerformanceReportGenerator extends AbstractReportGenerator<List<Grade>> {

    /** Нижня межа оцінки «добре» (ЄКТС C) для показника якості знань. */
    static final int QUALITY_SCORE = 74;

    private final GradeRepository gradeRepository;

    public GroupPerformanceReportGenerator(GradeRepository gradeRepository) {
        this.gradeRepository = gradeRepository;
    }

    @Override
    public ReportType type() {
        return ReportType.GROUP_PERFORMANCE;
    }

    @Override
    protected List<Grade> loadData(ReportRequest request) {
        return gradeRepository.findAllForReports().stream()
                .filter(grade -> matches(grade.getAssignment(), request))
                .toList();
    }

    @Override
    protected List<ReportColumn> columns() {
        return List.of(
                ReportColumn.text("group", "Група"),
                ReportColumn.number("course", "Курс"),
                ReportColumn.text("specialty", "Спеціальність"),
                ReportColumn.number("students", "Студентів"),
                ReportColumn.number("grades", "Підсумкових оцінок"),
                ReportColumn.number("averageScore", "Середній бал"),
                ReportColumn.number("passRate", "Успішність, %"),
                ReportColumn.number("qualityRate", "Якість знань, %"),
                ReportColumn.number("failed", "Незадовільних"));
    }

    @Override
    protected List<Map<String, Object>> aggregate(List<Grade> grades, ReportRequest request) {
        Map<StudyGroup, List<Grade>> byGroup = new TreeMap<>(Comparator
                .comparing(StudyGroup::getCourse)
                .thenComparing(StudyGroup::getName));
        for (Grade grade : grades) {
            byGroup.computeIfAbsent(grade.getAssignment().getGroup(), g -> new ArrayList<>()).add(grade);
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        byGroup.forEach((group, groupGrades) -> {
            List<Grade> finals = ReportStatistics.finalGrades(groupGrades);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("group", group.getName());
            row.put("course", group.getCourse());
            row.put("specialty", group.getSpecialty().getCode() + " " + group.getSpecialty().getName());
            row.put("students", finals.stream().map(g -> g.getStudent().getId()).distinct().count());
            row.put("grades", finals.size());
            row.put("averageScore", ReportStatistics.average(finals));
            row.put("passRate", ReportStatistics.passRate(finals));
            row.put("qualityRate", qualityRate(finals));
            row.put("failed", finals.stream().filter(g -> !g.isPassed()).count());
            rows.add(row);
        });
        return rows;
    }

    @Override
    protected Map<String, Object> summarize(List<Grade> grades, List<Map<String, Object>> rows) {
        List<Grade> finals = ReportStatistics.finalGrades(grades);
        Map<Integer, List<Grade>> byCourse = new TreeMap<>();
        for (Grade grade : finals) {
            byCourse.computeIfAbsent(grade.getAssignment().getGroup().getCourse(), c -> new ArrayList<>()).add(grade);
        }
        List<Map<String, Object>> courses = new ArrayList<>();
        byCourse.forEach((course, courseGrades) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("course", course);
            item.put("groups", courseGrades.stream().map(g -> g.getAssignment().getGroup().getId()).distinct().count());
            item.put("averageScore", ReportStatistics.average(courseGrades));
            item.put("passRate", ReportStatistics.passRate(courseGrades));
            item.put("qualityRate", qualityRate(courseGrades));
            courses.add(item);
        });
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("groups", rows.size());
        summary.put("averageScore", ReportStatistics.average(finals));
        summary.put("passRate", ReportStatistics.passRate(finals));
        summary.put("qualityRate", qualityRate(finals));
        summary.put("byCourse", courses);
        return summary;
    }

    private static Object qualityRate(List<Grade> finals) {
        long good = finals.stream().filter(g -> g.getScore() >= QUALITY_SCORE).count();
        return ReportStatistics.percent(good, finals.size());
    }

    static boolean matches(TeachingAssignment assignment, ReportRequest request) {
        StudyGroup group = assignment.getGroup();
        return request.matchesYear(assignment.getAcademicYear())
                && request.matchesSemester(assignment.getSemester())
                && request.matchesCourse(group.getCourse())
                && request.matchesFaculty(group.getSpecialty().getFaculty().getId());
    }
}
