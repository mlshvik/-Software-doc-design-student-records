package ua.lpnu.studentrecords.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ua.lpnu.studentrecords.domain.entity.Grade;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.entity.Teacher;
import ua.lpnu.studentrecords.domain.entity.TeachingAssignment;
import ua.lpnu.studentrecords.domain.enums.ControlType;
import ua.lpnu.studentrecords.domain.enums.GradeAttempt;
import ua.lpnu.studentrecords.domain.enums.StudentStatus;
import ua.lpnu.studentrecords.domain.exception.GradeValidationException;
import ua.lpnu.studentrecords.domain.exception.NotFoundException;
import ua.lpnu.studentrecords.pattern.chain.GradeValidationChainBuilder;
import ua.lpnu.studentrecords.pattern.observer.GradeEventPublisher;
import ua.lpnu.studentrecords.pattern.observer.GradeRecordedEvent;
import ua.lpnu.studentrecords.repository.GradeRepository;
import ua.lpnu.studentrecords.repository.StudentRepository;
import ua.lpnu.studentrecords.repository.TeacherRepository;
import ua.lpnu.studentrecords.repository.TeachingAssignmentRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static ua.lpnu.studentrecords.support.TestData.assignment;
import static ua.lpnu.studentrecords.support.TestData.grade;
import static ua.lpnu.studentrecords.support.TestData.group;
import static ua.lpnu.studentrecords.support.TestData.student;
import static ua.lpnu.studentrecords.support.TestData.subject;
import static ua.lpnu.studentrecords.support.TestData.teacher;

/**
 * Тести сценарію виставлення оцінки: ланцюжок валідації + збереження + сповіщення спостерігачів.
 */
class GradeServiceTest {

    private GradeRepository gradeRepository;
    private StudentRepository studentRepository;
    private TeachingAssignmentRepository assignmentRepository;
    private TeacherRepository teacherRepository;
    private GradeEventPublisher publisher;
    private GradeService service;

    private Student student;
    private Teacher teacher;
    private TeachingAssignment assignment;

    @BeforeEach
    void setUp() {
        gradeRepository = mock(GradeRepository.class);
        studentRepository = mock(StudentRepository.class);
        assignmentRepository = mock(TeachingAssignmentRepository.class);
        teacherRepository = mock(TeacherRepository.class);
        publisher = mock(GradeEventPublisher.class);
        service = new GradeService(gradeRepository, studentRepository, assignmentRepository, teacherRepository,
                new GradeValidationChainBuilder(), publisher);

        student = student(1L, group(1L, "ІР-43"));
        teacher = teacher(5L, "Коваль");
        assignment = assignment(3L, subject(1L, "Бази даних", "5.0", ControlType.EXAM), teacher, student.getGroup());

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(assignmentRepository.findById(3L)).thenReturn(Optional.of(assignment));
        when(teacherRepository.findById(5L)).thenReturn(Optional.of(teacher));
        when(gradeRepository.findByStudentIdAndAssignmentIdOrderByGradedAtAsc(1L, 3L)).thenReturn(List.of());
        when(gradeRepository.save(any(Grade.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void validGradeIsSavedAndObserversAreNotified() {
        Grade saved = service.recordGrade(new RecordGradeCommand(1L, 3L, 88, GradeAttempt.REGULAR, 5L));

        assertEquals(88, saved.getScore());
        assertSame(teacher, saved.getGradedBy());
        ArgumentCaptor<GradeRecordedEvent> captor = ArgumentCaptor.forClass(GradeRecordedEvent.class);
        verify(publisher).notifyListeners(captor.capture());
        assertSame(saved, captor.getValue().grade());
        assertSame(student, captor.getValue().student());
    }

    @Test
    void attemptIsDetectedAutomatically() {
        when(gradeRepository.findByStudentIdAndAssignmentIdOrderByGradedAtAsc(1L, 3L))
                .thenReturn(List.of(grade(student, assignment, 35, GradeAttempt.REGULAR)));

        Grade saved = service.recordGrade(new RecordGradeCommand(1L, 3L, 66, null, 5L));

        assertEquals(GradeAttempt.RETAKE_TEACHER, saved.getAttempt());
    }

    @Test
    void administratorEntryIsAttributedToAssignmentTeacher() {
        Grade saved = service.recordGrade(new RecordGradeCommand(1L, 3L, 75, GradeAttempt.REGULAR, null));

        assertSame(teacher, saved.getGradedBy());
    }

    @Test
    void rejectedGradeIsNotSavedAndNotPublished() {
        student.setStatus(StudentStatus.EXPELLED);

        assertThrows(GradeValidationException.class,
                () -> service.recordGrade(new RecordGradeCommand(1L, 3L, 75, GradeAttempt.REGULAR, 5L)));
        verify(gradeRepository, never()).save(any(Grade.class));
        verifyNoInteractions(publisher);
    }

    @Test
    void unknownStudentCausesNotFound() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.recordGrade(new RecordGradeCommand(99L, 3L, 75, GradeAttempt.REGULAR, 5L)));
    }

    @Test
    void nextAttemptIsCalculatedFromHistory() {
        when(gradeRepository.findByStudentIdAndAssignmentIdOrderByGradedAtAsc(1L, 3L)).thenReturn(List.of(
                grade(student, assignment, 35, GradeAttempt.REGULAR),
                grade(student, assignment, 41, GradeAttempt.RETAKE_TEACHER)));

        assertEquals(Optional.of(GradeAttempt.RETAKE_COMMISSION), service.nextAttempt(1L, 3L));
    }
}
