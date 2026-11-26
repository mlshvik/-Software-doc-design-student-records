package ua.lpnu.studentrecords.pattern.chain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.lpnu.studentrecords.domain.entity.Grade;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.entity.StudyGroup;
import ua.lpnu.studentrecords.domain.entity.Teacher;
import ua.lpnu.studentrecords.domain.entity.TeachingAssignment;
import ua.lpnu.studentrecords.domain.enums.ControlType;
import ua.lpnu.studentrecords.domain.enums.GradeAttempt;
import ua.lpnu.studentrecords.domain.enums.StudentStatus;
import ua.lpnu.studentrecords.domain.exception.GradeValidationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ua.lpnu.studentrecords.support.TestData.assignment;
import static ua.lpnu.studentrecords.support.TestData.grade;
import static ua.lpnu.studentrecords.support.TestData.group;
import static ua.lpnu.studentrecords.support.TestData.student;
import static ua.lpnu.studentrecords.support.TestData.subject;
import static ua.lpnu.studentrecords.support.TestData.teacher;

/**
 * Тести шаблону Chain of Responsibility – ланцюжка валідації оцінки.
 */
class GradeValidationChainTest {

    private GradeValidationHandler chain;
    private StudyGroup group;
    private Teacher teacher;
    private TeachingAssignment assignment;
    private Student student;

    @BeforeEach
    void setUp() {
        chain = new GradeValidationChainBuilder().build();
        group = group(1L, "ІР-43");
        teacher = teacher(1L, "Коваль");
        assignment = assignment(1L, subject(1L, "Бази даних", "5.0", ControlType.EXAM), teacher, group);
        student = student(1L, group);
    }

    private GradeValidationContext request(Integer score, GradeAttempt attempt, List<Grade> previous) {
        return new GradeValidationContext(student, assignment, teacher, score, attempt, previous);
    }

    private String rejectedBy(GradeValidationContext context) {
        return assertThrows(GradeValidationException.class, () -> chain.handle(context)).getRule();
    }

    @Test
    void builderAssemblesHandlersInOrder() {
        assertInstanceOf(ScoreRangeHandler.class, chain);
        GradeValidationHandler second = chain.getNext();
        assertInstanceOf(StudentActiveHandler.class, second);
        GradeValidationHandler third = second.getNext();
        assertInstanceOf(TeacherOwnsAssignmentHandler.class, third);
        GradeValidationHandler fourth = third.getNext();
        assertInstanceOf(AttemptLimitHandler.class, fourth);
        assertNull(fourth.getNext());
    }

    @Test
    void validRegularGradePassesWholeChain() {
        assertDoesNotThrow(() -> chain.handle(request(85, GradeAttempt.REGULAR, List.of())));
    }

    @Test
    void scoreOutOfRangeIsRejected() {
        assertEquals("ScoreRangeHandler", rejectedBy(request(101, GradeAttempt.REGULAR, List.of())));
        assertEquals("ScoreRangeHandler", rejectedBy(request(-5, GradeAttempt.REGULAR, List.of())));
        assertEquals("ScoreRangeHandler", rejectedBy(request(null, GradeAttempt.REGULAR, List.of())));
    }

    @Test
    void firstFailingHandlerStopsTheChain() {
        student.setStatus(StudentStatus.EXPELLED);
        assertEquals("ScoreRangeHandler", rejectedBy(request(150, GradeAttempt.REGULAR, List.of())));
    }

    @Test
    void expelledStudentCannotBeGraded() {
        student.setStatus(StudentStatus.EXPELLED);
        assertEquals("StudentActiveHandler", rejectedBy(request(75, GradeAttempt.REGULAR, List.of())));
    }

    @Test
    void studentOnAcademicLeaveCannotBeGraded() {
        student.setStatus(StudentStatus.ACADEMIC_LEAVE);
        assertEquals("StudentActiveHandler", rejectedBy(request(75, GradeAttempt.REGULAR, List.of())));
    }

    @Test
    void studentFromAnotherGroupIsRejected() {
        student.setGroup(group(2L, "ІР-42"));
        assertEquals("StudentActiveHandler", rejectedBy(request(75, GradeAttempt.REGULAR, List.of())));
    }

    @Test
    void foreignTeacherIsRejected() {
        GradeValidationContext context = new GradeValidationContext(
                student, assignment, teacher(2L, "Петренко"), 75, GradeAttempt.REGULAR, List.of());
        assertEquals("TeacherOwnsAssignmentHandler", rejectedBy(context));
    }

    @Test
    void administratorMayRecordGradeWithoutTeacher() {
        GradeValidationContext context = new GradeValidationContext(
                student, assignment, null, 75, GradeAttempt.REGULAR, List.of());
        assertDoesNotThrow(() -> chain.handle(context));
    }

    @Test
    void retakeWithoutRegularAttemptIsRejected() {
        assertEquals("AttemptLimitHandler", rejectedBy(request(70, GradeAttempt.RETAKE_TEACHER, List.of())));
    }

    @Test
    void retakeAfterFailedAttemptIsAllowed() {
        List<Grade> previous = List.of(grade(student, assignment, 45, GradeAttempt.REGULAR));
        assertDoesNotThrow(() -> chain.handle(request(70, GradeAttempt.RETAKE_TEACHER, previous)));
    }

    @Test
    void commissionOnlyAfterFailedTeacherRetake() {
        List<Grade> onlyRegular = List.of(grade(student, assignment, 45, GradeAttempt.REGULAR));
        assertEquals("AttemptLimitHandler", rejectedBy(request(70, GradeAttempt.RETAKE_COMMISSION, onlyRegular)));

        List<Grade> twoFailed = List.of(
                grade(student, assignment, 45, GradeAttempt.REGULAR),
                grade(student, assignment, 50, GradeAttempt.RETAKE_TEACHER));
        assertDoesNotThrow(() -> chain.handle(request(64, GradeAttempt.RETAKE_COMMISSION, twoFailed)));
    }

    @Test
    void passedSubjectCannotBeRegradedAgain() {
        List<Grade> previous = List.of(grade(student, assignment, 82, GradeAttempt.REGULAR));
        assertEquals("AttemptLimitHandler", rejectedBy(request(95, GradeAttempt.RETAKE_TEACHER, previous)));
    }

    @Test
    void attemptsAreExhaustedAfterCommission() {
        List<Grade> previous = List.of(
                grade(student, assignment, 20, GradeAttempt.REGULAR),
                grade(student, assignment, 30, GradeAttempt.RETAKE_TEACHER),
                grade(student, assignment, 40, GradeAttempt.RETAKE_COMMISSION));
        assertEquals("AttemptLimitHandler", rejectedBy(request(70, GradeAttempt.RETAKE_COMMISSION, previous)));
    }

    @Test
    void expectedAttemptFollowsHistory() {
        assertEquals(Optional.of(GradeAttempt.REGULAR), AttemptLimitHandler.expectedAttempt(List.of()));
        assertEquals(Optional.of(GradeAttempt.RETAKE_TEACHER), AttemptLimitHandler.expectedAttempt(
                List.of(grade(student, assignment, 30, GradeAttempt.REGULAR))));
        assertEquals(Optional.empty(), AttemptLimitHandler.expectedAttempt(
                List.of(grade(student, assignment, 60, GradeAttempt.REGULAR))));
    }
}
