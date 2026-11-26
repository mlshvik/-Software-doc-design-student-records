package ua.lpnu.studentrecords.service.schedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.lpnu.studentrecords.domain.entity.ScheduleEntry;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.entity.StudyGroup;
import ua.lpnu.studentrecords.domain.entity.TeachingAssignment;
import ua.lpnu.studentrecords.domain.enums.ControlType;
import ua.lpnu.studentrecords.domain.enums.LessonType;
import ua.lpnu.studentrecords.domain.enums.StudentStatus;
import ua.lpnu.studentrecords.domain.enums.WeekType;
import ua.lpnu.studentrecords.repository.ScheduleEntryRepository;
import ua.lpnu.studentrecords.repository.StudentRepository;
import ua.lpnu.studentrecords.repository.TeachingAssignmentRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ua.lpnu.studentrecords.support.TestData.assignment;
import static ua.lpnu.studentrecords.support.TestData.entry;
import static ua.lpnu.studentrecords.support.TestData.group;
import static ua.lpnu.studentrecords.support.TestData.student;
import static ua.lpnu.studentrecords.support.TestData.subject;
import static ua.lpnu.studentrecords.support.TestData.teacher;

/**
 * Тести сервісу розкладу.
 */
class ScheduleServiceTest {

    private ScheduleEntryRepository scheduleRepository;
    private StudentRepository studentRepository;
    private ScheduleService service;
    private StudyGroup group;
    private TeachingAssignment assignment;

    @BeforeEach
    void setUp() {
        scheduleRepository = mock(ScheduleEntryRepository.class);
        studentRepository = mock(StudentRepository.class);
        service = new ScheduleService(scheduleRepository, studentRepository,
                mock(TeachingAssignmentRepository.class), new ScheduleConflictChecker());
        group = group(1L, "ІР-43");
        assignment = assignment(1L, subject(1L, "Бази даних", "5.0", ControlType.EXAM), teacher(1L, "Коваль"), group);
    }

    @Test
    void groupScheduleIsSortedByDayAndPair() {
        ScheduleEntry tuesday = entry(1L, assignment, DayOfWeek.TUESDAY, 1, WeekType.ALL, "213", LessonType.LAB);
        ScheduleEntry mondayLate = entry(2L, assignment, DayOfWeek.MONDAY, 3, WeekType.ALL, "213", LessonType.LAB);
        ScheduleEntry mondayEarly = entry(3L, assignment, DayOfWeek.MONDAY, 1, WeekType.ALL, "213", LessonType.LECTURE);
        when(scheduleRepository.findByGroupId(1L)).thenReturn(List.of(tuesday, mondayLate, mondayEarly));

        assertEquals(List.of(mondayEarly, mondayLate, tuesday), service.getGroupSchedule(1L));
    }

    @Test
    void studentOnAcademicLeaveHasNoSchedule() {
        Student student = student(7L, group);
        student.setStatus(StudentStatus.ACADEMIC_LEAVE);
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student));

        assertTrue(service.getStudentSchedule(7L).isEmpty());
        verify(scheduleRepository, never()).findByGroupId(1L);
    }

    @Test
    void conflictingEntryIsNotSaved() {
        ScheduleEntry existing = entry(1L, assignment, DayOfWeek.MONDAY, 2, WeekType.ALL, "213", LessonType.LAB);
        when(scheduleRepository.findAtTime(DayOfWeek.MONDAY, 2)).thenReturn(List.of(existing));
        ScheduleEntry candidate = new ScheduleEntry(assignment, DayOfWeek.MONDAY, 2, WeekType.ODD, "305", LessonType.PRACTICE);

        ScheduleConflictException e = assertThrows(ScheduleConflictException.class, () -> service.create(candidate));
        assertEquals(2, e.getConflicts().size());
        verify(scheduleRepository, never()).save(any(ScheduleEntry.class));
    }

    @Test
    void freeSlotIsSaved() {
        when(scheduleRepository.findAtTime(DayOfWeek.FRIDAY, 4)).thenReturn(List.of());
        ScheduleEntry candidate = new ScheduleEntry(assignment, DayOfWeek.FRIDAY, 4, WeekType.ALL, "305", LessonType.PRACTICE);
        when(scheduleRepository.save(candidate)).thenReturn(candidate);

        assertSame(candidate, service.create(candidate));
    }
}
