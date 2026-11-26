package ua.lpnu.studentrecords.pattern.facade;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lpnu.studentrecords.domain.entity.AcademicDebt;
import ua.lpnu.studentrecords.domain.entity.Grade;
import ua.lpnu.studentrecords.domain.entity.ScheduleEntry;
import ua.lpnu.studentrecords.domain.entity.Specialty;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.entity.StudyGroup;
import ua.lpnu.studentrecords.domain.entity.Subject;
import ua.lpnu.studentrecords.domain.entity.TeachingAssignment;
import ua.lpnu.studentrecords.domain.enums.DebtStatus;
import ua.lpnu.studentrecords.pattern.scale.GradingScale;
import ua.lpnu.studentrecords.pattern.scale.ScaleGrade;
import ua.lpnu.studentrecords.pattern.strategy.FinalGradeSelector;
import ua.lpnu.studentrecords.service.DebtService;
import ua.lpnu.studentrecords.service.GpaService;
import ua.lpnu.studentrecords.service.GradeService;
import ua.lpnu.studentrecords.service.StudentService;
import ua.lpnu.studentrecords.service.schedule.ScheduleService;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Єдина точка доступу до «залікової книжки» студента.
 * <p>
 * <b>Шаблон Facade.</b> Кабінету студента одночасно потрібні оцінки (з перекладом у шкали
 * ЄКТС і національну), середній бал, заборгованості та розклад. Ці дані надають п'ять різних
 * підсистем ({@link StudentService}, {@link GradeService}, {@link GpaService},
 * {@link DebtService}, {@link ScheduleService}) і {@link GradingScale}. Фасад приховує цю
 * складність за одним методом {@link #getRecord(Long)}: клієнт (REST-контролер, етап 4)
 * робить один виклик і отримує готовий {@link StudentRecordDto}, а всі дані читаються
 * в одній транзакції.
 */
@Service
@Transactional(readOnly = true)
public class StudentRecordFacade {

    private final StudentService studentService;
    private final GradeService gradeService;
    private final GpaService gpaService;
    private final DebtService debtService;
    private final ScheduleService scheduleService;

    public StudentRecordFacade(StudentService studentService,
                               GradeService gradeService,
                               GpaService gpaService,
                               DebtService debtService,
                               ScheduleService scheduleService) {
        this.studentService = studentService;
        this.gradeService = gradeService;
        this.gpaService = gpaService;
        this.debtService = debtService;
        this.scheduleService = scheduleService;
    }

    public StudentRecordDto getRecord(Long studentId) {
        Student student = studentService.getById(studentId);
        List<Grade> grades = gradeService.getStudentGrades(studentId);
        Set<Grade> finals = Collections.newSetFromMap(new IdentityHashMap<>());
        finals.addAll(FinalGradeSelector.selectFinal(grades));

        List<StudentRecordDto.GradeItem> gradeItems = grades.stream()
                .map(grade -> toGradeItem(grade, finals.contains(grade)))
                .toList();
        List<StudentRecordDto.DebtItem> debtItems = debtService.getStudentDebts(studentId).stream()
                .map(StudentRecordFacade::toDebtItem)
                .toList();
        long openDebts = debtItems.stream().filter(d -> d.status() == DebtStatus.OPEN).count();
        List<StudentRecordDto.ScheduleItem> schedule = scheduleService.getStudentSchedule(studentId).stream()
                .map(StudentRecordFacade::toScheduleItem)
                .toList();

        return new StudentRecordDto(toStudentInfo(student), gradeItems, gpaService.calculate(grades),
                debtItems, openDebts, schedule);
    }

    private static StudentRecordDto.StudentInfo toStudentInfo(Student student) {
        StudyGroup group = student.getGroup();
        Specialty specialty = group.getSpecialty();
        return new StudentRecordDto.StudentInfo(
                student.getId(),
                student.getFullName(),
                student.getRecordBookNumber(),
                group.getName(),
                group.getCourse(),
                specialty.getCode() + " " + specialty.getName(),
                specialty.getFaculty().getShortName(),
                student.getStatus(),
                student.getState().title());
    }

    private static StudentRecordDto.GradeItem toGradeItem(Grade grade, boolean finalResult) {
        TeachingAssignment assignment = grade.getAssignment();
        Subject subject = assignment.getSubject();
        ScaleGrade scale = GradingScale.INSTANCE.convert(grade.getScore(), subject.getControlType());
        return new StudentRecordDto.GradeItem(
                grade.getId(),
                assignment.getId(),
                subject.getName(),
                subject.getControlType(),
                subject.getCredits(),
                assignment.getSemester(),
                assignment.getAcademicYear(),
                assignment.getTeacher().getShortName(),
                grade.getScore(),
                grade.getAttempt(),
                scale.national(),
                scale.ects(),
                scale.passed(),
                finalResult,
                grade.getGradedAt());
    }

    private static StudentRecordDto.DebtItem toDebtItem(AcademicDebt debt) {
        TeachingAssignment assignment = debt.getAssignment();
        return new StudentRecordDto.DebtItem(
                debt.getId(),
                assignment.getId(),
                assignment.getSubject().getName(),
                assignment.getSemester(),
                assignment.getAcademicYear(),
                assignment.getTeacher().getShortName(),
                debt.getStatus(),
                debt.getOpenedAt(),
                debt.getClosedAt());
    }

    private static StudentRecordDto.ScheduleItem toScheduleItem(ScheduleEntry entry) {
        TeachingAssignment assignment = entry.getAssignment();
        return new StudentRecordDto.ScheduleItem(
                entry.getId(),
                entry.getDayOfWeek(),
                entry.getPairNumber(),
                entry.getWeekType(),
                assignment.getSubject().getName(),
                assignment.getTeacher().getShortName(),
                entry.getClassroom(),
                entry.getLessonType());
    }
}
