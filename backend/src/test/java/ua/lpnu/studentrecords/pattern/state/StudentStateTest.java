package ua.lpnu.studentrecords.pattern.state;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.enums.StudentStatus;
import ua.lpnu.studentrecords.domain.exception.InvalidStatusTransitionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ua.lpnu.studentrecords.support.TestData.group;
import static ua.lpnu.studentrecords.support.TestData.student;

/**
 * Тести шаблону State – поведінки і переходів між статусами студента.
 */
class StudentStateTest {

    @Test
    void factoryReturnsStateForEachStatus() {
        assertInstanceOf(ActiveState.class, StudentStateFactory.forStatus(StudentStatus.ACTIVE));
        assertInstanceOf(AcademicLeaveState.class, StudentStateFactory.forStatus(StudentStatus.ACADEMIC_LEAVE));
        assertInstanceOf(ExpelledState.class, StudentStateFactory.forStatus(StudentStatus.EXPELLED));
        assertInstanceOf(GraduatedState.class, StudentStateFactory.forStatus(StudentStatus.GRADUATED));
    }

    @ParameterizedTest
    @EnumSource(StudentStatus.class)
    void stateMatchesItsStatus(StudentStatus status) {
        assertEquals(status, StudentStateFactory.forStatus(status).status());
    }

    @Test
    void activeStudentCanBeGradedAndAttendClasses() {
        StudentState state = StudentStateFactory.forStatus(StudentStatus.ACTIVE);
        assertTrue(state.canBeGraded());
        assertTrue(state.canAttendClasses());
    }

    @ParameterizedTest
    @EnumSource(value = StudentStatus.class, names = {"ACADEMIC_LEAVE", "EXPELLED", "GRADUATED"})
    void inactiveStudentCannotBeGraded(StudentStatus status) {
        StudentState state = StudentStateFactory.forStatus(status);
        assertFalse(state.canBeGraded());
        assertFalse(state.canAttendClasses());
    }

    @Test
    void studentGoesOnAcademicLeaveAndReturns() {
        Student student = student(1L, group(1L, "ІР-43"));
        student.changeStatus(StudentStatus.ACADEMIC_LEAVE);
        assertEquals(StudentStatus.ACADEMIC_LEAVE, student.getStatus());
        assertFalse(student.canBeGraded());

        student.changeStatus(StudentStatus.ACTIVE);
        assertEquals(StudentStatus.ACTIVE, student.getStatus());
        assertTrue(student.canBeGraded());
    }

    @Test
    void expelledStudentCanOnlyBeReinstated() {
        Student student = student(1L, group(1L, "ІР-43"));
        student.changeStatus(StudentStatus.EXPELLED);
        assertThrows(InvalidStatusTransitionException.class,
                () -> student.changeStatus(StudentStatus.ACADEMIC_LEAVE));
        student.changeStatus(StudentStatus.ACTIVE);
        assertEquals(StudentStatus.ACTIVE, student.getStatus());
    }

    @ParameterizedTest
    @EnumSource(StudentStatus.class)
    void graduatedIsFinalState(StudentStatus target) {
        Student student = student(1L, group(1L, "ІР-43"));
        student.changeStatus(StudentStatus.GRADUATED);
        InvalidStatusTransitionException e = assertThrows(InvalidStatusTransitionException.class,
                () -> student.changeStatus(target));
        assertEquals(StudentStatus.GRADUATED, e.getFrom());
        assertEquals(target, e.getTo());
        assertEquals(StudentStatus.GRADUATED, student.getStatus());
    }

    @Test
    void transitionToSameStatusIsRejected() {
        Student student = student(1L, group(1L, "ІР-43"));
        assertThrows(InvalidStatusTransitionException.class, () -> student.changeStatus(StudentStatus.ACTIVE));
    }

    @Test
    void nullTargetIsRejected() {
        StudentState state = StudentStateFactory.forStatus(StudentStatus.ACTIVE);
        assertThrows(InvalidStatusTransitionException.class, () -> state.transitionTo(null));
    }
}
