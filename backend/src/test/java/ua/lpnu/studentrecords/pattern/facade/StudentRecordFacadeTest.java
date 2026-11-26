package ua.lpnu.studentrecords.pattern.facade;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.lpnu.studentrecords.domain.entity.AcademicDebt;
import ua.lpnu.studentrecords.domain.entity.Grade;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.entity.Teacher;
import ua.lpnu.studentrecords.domain.entity.TeachingAssignment;
import ua.lpnu.studentrecords.domain.enums.ControlType;
import ua.lpnu.studentrecords.domain.enums.GradeAttempt;
import ua.lpnu.studentrecords.domain.enums.LessonType;
import ua.lpnu.studentrecords.domain.enums.WeekType;
import ua.lpnu.studentrecords.pattern.strategy.GpaStrategyType;
import ua.lpnu.studentrecords.service.DebtService;
import ua.lpnu.studentrecords.service.GpaResult;
import ua.lpnu.studentrecords.service.GpaService;
import ua.lpnu.studentrecords.service.GradeService;
import ua.lpnu.studentrecords.service.StudentService;
import ua.lpnu.studentrecords.service.schedule.ScheduleService;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ua.lpnu.studentrecords.support.TestData.assignment;
import static ua.lpnu.studentrecords.support.TestData.entry;
import static ua.lpnu.studentrecords.support.TestData.grade;
import static ua.lpnu.studentrecords.support.TestData.group;
import static ua.lpnu.studentrecords.support.TestData.student;
import static ua.lpnu.studentrecords.support.TestData.subject;
import static ua.lpnu.studentrecords.support.TestData.teacher;

/**
 * Тести шаблону Facade: один виклик збирає дані з п'яти підсистем.
 */
class StudentRecordFacadeTest {

    private GpaService gpaService;
    private StudentRecordFacade facade;
    private List<Grade> grades;
    private GpaResult gpa;

    @BeforeEach
    void setUp() {
        StudentService studentService = mock(StudentService.class);
        GradeService gradeService = mock(GradeService.class);
        gpaService = mock(GpaService.class);
        DebtService debtService = mock(DebtService.class);
        ScheduleService scheduleService = mock(ScheduleService.class);
        facade = new StudentRecordFacade(studentService, gradeService, gpaService, debtService, scheduleService);

        Student student = student(1L, group(1L, "ІР-43"));
        Teacher teacher = teacher(1L, "Коваль");
        TeachingAssignment databases = assignment(1L, subject(1L, "Бази даних", "5.0", ControlType.EXAM), teacher, student.getGroup());
        TeachingAssignment philosophy = assignment(2L, subject(2L, "Філософія", "3.0", ControlType.CREDIT), teacher, student.getGroup());
        grades = List.of(
                grade(student, databases, 91, GradeAttempt.REGULAR),
                grade(student, philosophy, 30, GradeAttempt.REGULAR),
                grade(student, philosophy, 61, GradeAttempt.RETAKE_TEACHER));
        AcademicDebt closed = new AcademicDebt(student, philosophy);
        closed.close();
        AcademicDebt open = new AcademicDebt(student, databases);
        gpa = new GpaResult(new BigDecimal("76.00"), "C", GpaStrategyType.SIMPLE, 2, new BigDecimal("8.0"));

        when(studentService.getById(1L)).thenReturn(student);
        when(gradeService.getStudentGrades(1L)).thenReturn(grades);
        when(gpaService.calculate(grades)).thenReturn(gpa);
        when(debtService.getStudentDebts(1L)).thenReturn(List.of(open, closed));
        when(scheduleService.getStudentSchedule(1L)).thenReturn(List.of(
                entry(1L, databases, DayOfWeek.MONDAY, 2, WeekType.ALL, "213", LessonType.LAB)));
    }

    @Test
    void collectsWholeRecordInOneCall() {
        StudentRecordDto record = facade.getRecord(1L);

        assertEquals("Маліш Вікторія Олегівна", record.student().fullName());
        assertEquals("ІР-43", record.student().groupName());
        assertEquals("ІКНІ", record.student().faculty());
        assertEquals("Навчається", record.student().statusTitle());
        assertEquals(3, record.grades().size());
        assertSame(gpa, record.gpa());
        assertEquals(2, record.debts().size());
        assertEquals(1, record.openDebts());
        assertEquals(1, record.schedule().size());
        assertEquals("Коваль О. І.", record.schedule().get(0).teacher());
        verify(gpaService).calculate(grades);
    }

    @Test
    void gradesAreConvertedToAllScalesAndFinalAttemptIsMarked() {
        List<StudentRecordDto.GradeItem> items = facade.getRecord(1L).grades();

        StudentRecordDto.GradeItem exam = items.get(0);
        assertEquals("відмінно", exam.national());
        assertEquals("A", exam.ects());
        assertTrue(exam.finalResult());

        StudentRecordDto.GradeItem failedCredit = items.get(1);
        assertEquals("не зараховано", failedCredit.national());
        assertFalse(failedCredit.passed());
        assertFalse(failedCredit.finalResult());

        StudentRecordDto.GradeItem retake = items.get(2);
        assertEquals("зараховано", retake.national());
        assertEquals("E", retake.ects());
        assertTrue(retake.finalResult());
    }
}
