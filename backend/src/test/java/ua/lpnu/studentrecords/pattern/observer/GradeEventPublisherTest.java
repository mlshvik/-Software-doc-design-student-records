package ua.lpnu.studentrecords.pattern.observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.entity.TeachingAssignment;
import ua.lpnu.studentrecords.domain.enums.ControlType;
import ua.lpnu.studentrecords.domain.enums.GradeAttempt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static ua.lpnu.studentrecords.support.TestData.assignment;
import static ua.lpnu.studentrecords.support.TestData.grade;
import static ua.lpnu.studentrecords.support.TestData.group;
import static ua.lpnu.studentrecords.support.TestData.student;
import static ua.lpnu.studentrecords.support.TestData.subject;
import static ua.lpnu.studentrecords.support.TestData.teacher;

/**
 * Тести шаблону Observer – підписки і розсилки подій про нові оцінки.
 */
class GradeEventPublisherTest {

    private GradeEventListener first;
    private GradeEventListener second;
    private GradeEventPublisher publisher;
    private GradeRecordedEvent event;

    @BeforeEach
    void setUp() {
        first = mock(GradeEventListener.class);
        second = mock(GradeEventListener.class);
        publisher = new GradeEventPublisher(List.of(first, second));

        Student student = student(1L, group(1L, "ІР-43"));
        TeachingAssignment assignment = assignment(1L,
                subject(1L, "Бази даних", "5.0", ControlType.EXAM), teacher(1L, "Коваль"), student.getGroup());
        event = new GradeRecordedEvent(grade(student, assignment, 77, GradeAttempt.REGULAR), student);
    }

    @Test
    void notifiesAllSubscribersInOrder() {
        publisher.notifyListeners(event);

        InOrder order = inOrder(first, second);
        order.verify(first).onGradeRecorded(event);
        order.verify(second).onGradeRecorded(event);
    }

    @Test
    void unsubscribedListenerIsNotNotified() {
        publisher.unsubscribe(first);
        publisher.notifyListeners(event);

        verify(first, never()).onGradeRecorded(event);
        verify(second).onGradeRecorded(event);
    }

    @Test
    void listenerIsSubscribedOnlyOnce() {
        GradeEventListener third = mock(GradeEventListener.class);
        publisher.subscribe(third);
        publisher.subscribe(third);
        publisher.subscribe(first);

        assertEquals(3, publisher.getListeners().size());
    }
}
