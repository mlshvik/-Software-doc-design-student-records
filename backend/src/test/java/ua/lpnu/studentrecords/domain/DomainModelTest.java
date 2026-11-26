package ua.lpnu.studentrecords.domain;

import org.junit.jupiter.api.Test;
import ua.lpnu.studentrecords.domain.entity.AcademicDebt;
import ua.lpnu.studentrecords.domain.entity.Grade;
import ua.lpnu.studentrecords.domain.entity.ScheduleEntry;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.enums.DebtStatus;
import ua.lpnu.studentrecords.domain.enums.GradeAttempt;
import ua.lpnu.studentrecords.domain.enums.LessonType;
import ua.lpnu.studentrecords.domain.enums.WeekType;

import java.time.DayOfWeek;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Модульні тести поведінки доменної моделі (без Spring і БД).
 */
class DomainModelTest {

    @Test
    void gradeAttemptsFollowRetakeOrder() {
        assertEquals(GradeAttempt.RETAKE_TEACHER, GradeAttempt.REGULAR.next());
        assertEquals(GradeAttempt.RETAKE_COMMISSION, GradeAttempt.RETAKE_TEACHER.next());
        assertNull(GradeAttempt.RETAKE_COMMISSION.next());
    }

    @Test
    void weekTypesOverlapCorrectly() {
        assertTrue(WeekType.ALL.overlaps(WeekType.ODD));
        assertTrue(WeekType.EVEN.overlaps(WeekType.ALL));
        assertTrue(WeekType.ODD.overlaps(WeekType.ODD));
        assertFalse(WeekType.ODD.overlaps(WeekType.EVEN));
    }

    @Test
    void gradeIsPassedFromSixtyPoints() {
        assertTrue(new Grade(null, null, 60, GradeAttempt.REGULAR, null).isPassed());
        assertFalse(new Grade(null, null, 59, GradeAttempt.REGULAR, null).isPassed());
    }

    @Test
    void studentNamesAreFormatted() {
        Student student = new Student("Шевченко", "Тарас", "Григорович", "IR23999", null);
        assertEquals("Шевченко Тарас Григорович", student.getFullName());
        assertEquals("Шевченко Т. Г.", student.getShortName());
    }

    @Test
    void scheduleEntryDetectsTimeOverlap() {
        ScheduleEntry entry = new ScheduleEntry(null, DayOfWeek.MONDAY, 2, WeekType.ODD, "213", LessonType.LAB);
        assertTrue(entry.overlapsInTime(DayOfWeek.MONDAY, 2, WeekType.ALL));
        assertFalse(entry.overlapsInTime(DayOfWeek.MONDAY, 2, WeekType.EVEN));
        assertFalse(entry.overlapsInTime(DayOfWeek.TUESDAY, 2, WeekType.ODD));
    }

    @Test
    void debtCanBeClosed() {
        AcademicDebt debt = new AcademicDebt(null, null);
        assertTrue(debt.isOpen());
        debt.close();
        assertEquals(DebtStatus.CLOSED, debt.getStatus());
        assertNotNull(debt.getClosedAt());
    }
}
