package ua.lpnu.studentrecords.service.schedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.lpnu.studentrecords.domain.entity.ScheduleEntry;
import ua.lpnu.studentrecords.domain.entity.StudyGroup;
import ua.lpnu.studentrecords.domain.entity.Subject;
import ua.lpnu.studentrecords.domain.entity.Teacher;
import ua.lpnu.studentrecords.domain.entity.TeachingAssignment;
import ua.lpnu.studentrecords.domain.enums.ControlType;
import ua.lpnu.studentrecords.domain.enums.LessonType;
import ua.lpnu.studentrecords.domain.enums.WeekType;

import java.time.DayOfWeek;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ua.lpnu.studentrecords.support.TestData.assignment;
import static ua.lpnu.studentrecords.support.TestData.entry;
import static ua.lpnu.studentrecords.support.TestData.group;
import static ua.lpnu.studentrecords.support.TestData.subject;
import static ua.lpnu.studentrecords.support.TestData.teacher;

/**
 * Тести перевірки конфліктів у розкладі.
 */
class ScheduleConflictCheckerTest {

    private final ScheduleConflictChecker checker = new ScheduleConflictChecker();

    private StudyGroup ir43;
    private StudyGroup ir42;
    private Teacher koval;
    private Teacher petrenko;
    private Subject databases;
    private Subject philosophy;
    private ScheduleEntry existing;

    @BeforeEach
    void setUp() {
        ir43 = group(1L, "ІР-43");
        ir42 = group(2L, "ІР-42");
        koval = teacher(1L, "Коваль");
        petrenko = teacher(2L, "Петренко");
        databases = subject(1L, "Бази даних", "5.0", ControlType.EXAM);
        philosophy = subject(2L, "Філософія", "3.0", ControlType.CREDIT);
        existing = entry(1L, assignment(1L, databases, koval, ir43),
                DayOfWeek.MONDAY, 2, WeekType.ALL, "213", LessonType.LAB);
    }

    private ScheduleEntry candidate(TeachingAssignment assignment, WeekType weekType, String classroom,
                                    LessonType lessonType) {
        return new ScheduleEntry(assignment, DayOfWeek.MONDAY, 2, weekType, classroom, lessonType);
    }

    private List<ConflictType> conflictTypes(ScheduleEntry candidate, ScheduleEntry... entries) {
        return checker.findConflicts(candidate, List.of(entries)).stream().map(ScheduleConflict::type).toList();
    }

    @Test
    void teacherCannotTeachTwoClassesAtOnce() {
        ScheduleEntry candidate = candidate(assignment(2L, philosophy, koval, ir42), WeekType.ALL, "301", LessonType.PRACTICE);
        assertEquals(List.of(ConflictType.TEACHER), conflictTypes(candidate, existing));
    }

    @Test
    void classroomCannotBeUsedTwice() {
        ScheduleEntry candidate = candidate(assignment(2L, philosophy, petrenko, ir42), WeekType.ALL, " 213 ", LessonType.PRACTICE);
        assertEquals(List.of(ConflictType.CLASSROOM), conflictTypes(candidate, existing));
    }

    @Test
    void groupCannotHaveTwoClassesAtOnce() {
        ScheduleEntry candidate = candidate(assignment(2L, philosophy, petrenko, ir43), WeekType.ODD, "301", LessonType.LECTURE);
        assertEquals(List.of(ConflictType.GROUP), conflictTypes(candidate, existing));
    }

    @Test
    void reportsAllConflictsAtOnce() {
        ScheduleEntry candidate = candidate(assignment(2L, philosophy, koval, ir43), WeekType.ALL, "213", LessonType.PRACTICE);
        assertEquals(List.of(ConflictType.TEACHER, ConflictType.CLASSROOM, ConflictType.GROUP),
                conflictTypes(candidate, existing));
    }

    @Test
    void oddAndEvenWeeksDoNotOverlap() {
        ScheduleEntry oddEntry = entry(3L, assignment(1L, databases, koval, ir43),
                DayOfWeek.MONDAY, 2, WeekType.ODD, "213", LessonType.LAB);
        ScheduleEntry candidate = candidate(assignment(2L, philosophy, koval, ir43), WeekType.EVEN, "213", LessonType.LAB);
        assertTrue(conflictTypes(candidate, oddEntry).isEmpty());
    }

    @Test
    void differentPairIsNotAConflict() {
        ScheduleEntry candidate = new ScheduleEntry(assignment(2L, philosophy, koval, ir43),
                DayOfWeek.MONDAY, 3, WeekType.ALL, "213", LessonType.LAB);
        assertTrue(conflictTypes(candidate, existing).isEmpty());
    }

    @Test
    void streamLectureForSeveralGroupsIsAllowed() {
        ScheduleEntry lecture = entry(5L, assignment(5L, databases, koval, ir43),
                DayOfWeek.MONDAY, 2, WeekType.ALL, "Аудиторія 1", LessonType.LECTURE);
        ScheduleEntry candidate = candidate(assignment(6L, databases, koval, ir42), WeekType.ALL, "Аудиторія 1", LessonType.LECTURE);
        assertTrue(conflictTypes(candidate, lecture).isEmpty());
    }

    @Test
    void entryDoesNotConflictWithItselfWhenEdited() {
        ScheduleEntry edited = entry(1L, existing.getAssignment(), DayOfWeek.MONDAY, 2, WeekType.ALL, "213", LessonType.LAB);
        assertTrue(conflictTypes(edited, existing).isEmpty());
    }

    @Test
    void conflictMessageIsHumanReadable() {
        ScheduleEntry candidate = candidate(assignment(2L, philosophy, koval, ir42), WeekType.ALL, "301", LessonType.PRACTICE);
        String message = checker.findConflicts(candidate, List.of(existing)).get(0).message();
        assertTrue(message.contains("Коваль О. І."));
        assertTrue(message.contains("Бази даних"));
    }
}
