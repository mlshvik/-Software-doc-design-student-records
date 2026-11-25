package ua.lpnu.studentrecords.pattern.report;

import org.springframework.stereotype.Component;
import ua.lpnu.studentrecords.domain.entity.AcademicDebt;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.entity.StudyGroup;
import ua.lpnu.studentrecords.domain.entity.TeachingAssignment;
import ua.lpnu.studentrecords.repository.AcademicDebtRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Звіт «Академічні заборгованості» (конкретний клас шаблону Template Method).
 * <p>
 * Рядки – відкриті заборгованості (студент, група, дисципліна, викладач, скільки днів борг відкритий).
 * У підсумках – кількість відкритих і закритих боргів, студентів з боргами та розподіл за групами.
 */
@Component
public class AcademicDebtReportGenerator extends AbstractReportGenerator<List<AcademicDebt>> {

    private final AcademicDebtRepository debtRepository;

    public AcademicDebtReportGenerator(AcademicDebtRepository debtRepository) {
        this.debtRepository = debtRepository;
    }

    @Override
    public ReportType type() {
        return ReportType.ACADEMIC_DEBTS;
    }

    @Override
    protected List<AcademicDebt> loadData(ReportRequest request) {
        return debtRepository.findAllForReports().stream()
                .filter(debt -> matches(debt, request))
                .toList();
    }

    @Override
    protected List<ReportColumn> columns() {
        return List.of(
                ReportColumn.text("student", "Студент"),
                ReportColumn.text("recordBook", "Залікова книжка"),
                ReportColumn.text("group", "Група"),
                ReportColumn.number("course", "Курс"),
                ReportColumn.text("subject", "Дисципліна"),
                ReportColumn.text("teacher", "Викладач"),
                ReportColumn.number("semester", "Семестр"),
                ReportColumn.text("academicYear", "Навчальний рік"),
                ReportColumn.text("openedAt", "Відкрито"),
                ReportColumn.number("daysOpen", "Днів"));
    }

    @Override
    protected List<Map<String, Object>> aggregate(List<AcademicDebt> debts, ReportRequest request) {
        LocalDate today = LocalDate.now();
        return debts.stream()
                .filter(AcademicDebt::isOpen)
                .sorted(Comparator
                        .comparing((AcademicDebt d) -> d.getStudent().getGroup().getName())
                        .thenComparing(d -> d.getStudent().getLastName())
                        .thenComparing(AcademicDebt::getOpenedAt))
                .map(debt -> toRow(debt, today))
                .toList();
    }

    @Override
    protected Map<String, Object> summarize(List<AcademicDebt> debts, List<Map<String, Object>> rows) {
        Map<String, Long> byGroup = new TreeMap<>();
        debts.stream()
                .filter(AcademicDebt::isOpen)
                .forEach(debt -> byGroup.merge(debt.getStudent().getGroup().getName(), 1L, Long::sum));
        List<Map<String, Object>> groups = new ArrayList<>();
        byGroup.forEach((group, count) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("group", group);
            item.put("debts", count);
            groups.add(item);
        });
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("open", rows.size());
        summary.put("closed", debts.stream().filter(debt -> !debt.isOpen()).count());
        summary.put("studentsWithDebts", debts.stream()
                .filter(AcademicDebt::isOpen)
                .map(debt -> debt.getStudent().getId())
                .distinct()
                .count());
        summary.put("byGroup", groups);
        return summary;
    }

    private static Map<String, Object> toRow(AcademicDebt debt, LocalDate today) {
        Student student = debt.getStudent();
        StudyGroup group = student.getGroup();
        TeachingAssignment assignment = debt.getAssignment();
        LocalDate opened = debt.getOpenedAt().toLocalDate();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("student", student.getFullName());
        row.put("recordBook", student.getRecordBookNumber());
        row.put("group", group.getName());
        row.put("course", group.getCourse());
        row.put("subject", assignment.getSubject().getName());
        row.put("teacher", assignment.getTeacher().getShortName());
        row.put("semester", assignment.getSemester());
        row.put("academicYear", assignment.getAcademicYear());
        row.put("openedAt", opened.toString());
        row.put("daysOpen", Math.max(0, ChronoUnit.DAYS.between(opened, today)));
        return row;
    }

    private static boolean matches(AcademicDebt debt, ReportRequest request) {
        TeachingAssignment assignment = debt.getAssignment();
        StudyGroup group = debt.getStudent().getGroup();
        return request.matchesYear(assignment.getAcademicYear())
                && request.matchesSemester(assignment.getSemester())
                && request.matchesCourse(group.getCourse())
                && request.matchesFaculty(group.getSpecialty().getFaculty().getId());
    }
}
