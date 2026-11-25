package ua.lpnu.studentrecords.pattern.observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ua.lpnu.studentrecords.domain.entity.Notification;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.entity.TeachingAssignment;
import ua.lpnu.studentrecords.domain.enums.ControlType;
import ua.lpnu.studentrecords.domain.enums.GradeAttempt;
import ua.lpnu.studentrecords.pattern.strategy.GpaStrategyType;
import ua.lpnu.studentrecords.repository.NotificationRepository;
import ua.lpnu.studentrecords.service.DebtService;
import ua.lpnu.studentrecords.service.GpaResult;
import ua.lpnu.studentrecords.service.GpaService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static ua.lpnu.studentrecords.support.TestData.assignment;
import static ua.lpnu.studentrecords.support.TestData.grade;
import static ua.lpnu.studentrecords.support.TestData.group;
import static ua.lpnu.studentrecords.support.TestData.studentWithUser;
import static ua.lpnu.studentrecords.support.TestData.subject;
import static ua.lpnu.studentrecords.support.TestData.teacher;

/**
 * Тести конкретних спостерігачів (ConcreteObserver).
 */
class GradeObserversTest {

    private Student student;
    private TeachingAssignment assignment;

    @BeforeEach
    void setUp() {
        student = studentWithUser(1L, group(1L, "ІР-43"));
        assignment = assignment(1L, subject(1L, "Бази даних", "5.0", ControlType.EXAM),
                teacher(1L, "Коваль"), student.getGroup());
    }

    private GradeRecordedEvent event(int score, GradeAttempt attempt) {
        return new GradeRecordedEvent(grade(student, assignment, score, attempt), student);
    }

    @Test
    void failingGradeOpensDebt() {
        DebtService debtService = mock(DebtService.class);
        new AcademicDebtObserver(debtService).onGradeRecorded(event(42, GradeAttempt.REGULAR));

        verify(debtService).openDebt(student, assignment);
        verify(debtService, never()).closeDebt(student, assignment);
    }

    @Test
    void passingGradeClosesDebt() {
        DebtService debtService = mock(DebtService.class);
        new AcademicDebtObserver(debtService).onGradeRecorded(event(68, GradeAttempt.RETAKE_TEACHER));

        verify(debtService).closeDebt(student, assignment);
        verify(debtService, never()).openDebt(student, assignment);
    }

    @Test
    void studentReceivesNotificationAboutGrade() {
        NotificationRepository repository = mock(NotificationRepository.class);
        new NotificationObserver(repository).onGradeRecorded(event(85, GradeAttempt.REGULAR));

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(captor.capture());
        Notification notification = captor.getValue();
        assertSame(student.getUser(), notification.getRecipient());
        assertEquals("Нова оцінка з дисципліни «Бази даних»: 85 балів – добре (B).", notification.getMessage());
    }

    @Test
    void notificationAboutFailedRetakeMentionsDebt() {
        String message = new NotificationObserver(mock(NotificationRepository.class))
                .buildMessage(event(40, GradeAttempt.RETAKE_COMMISSION));

        assertTrue(message.contains("(перескладання комісії)"));
        assertTrue(message.contains("незадовільно (FX)"));
        assertTrue(message.contains("заборгованість"));
    }

    @Test
    void studentWithoutAccountIsNotNotified() {
        NotificationRepository repository = mock(NotificationRepository.class);
        student.setUser(null);
        new NotificationObserver(repository).onGradeRecorded(event(85, GradeAttempt.REGULAR));

        verifyNoInteractions(repository);
    }

    @Test
    void gpaIsRecalculatedAfterGrade() {
        GpaService gpaService = mock(GpaService.class);
        when(gpaService.recalculate(1L)).thenReturn(
                new GpaResult(new BigDecimal("85.00"), "B", GpaStrategyType.SIMPLE, 1, new BigDecimal("5.0")));

        new GpaRecalculationObserver(gpaService).onGradeRecorded(event(85, GradeAttempt.REGULAR));

        verify(gpaService).recalculate(1L);
    }
}
