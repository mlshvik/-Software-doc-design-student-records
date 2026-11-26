package ua.lpnu.studentrecords.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.enums.StudentStatus;
import ua.lpnu.studentrecords.domain.exception.InvalidStatusTransitionException;
import ua.lpnu.studentrecords.domain.exception.NotFoundException;
import ua.lpnu.studentrecords.repository.StudentRepository;
import ua.lpnu.studentrecords.repository.StudyGroupRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ua.lpnu.studentrecords.support.TestData.group;
import static ua.lpnu.studentrecords.support.TestData.student;

/**
 * Тести зміни статусу студента через сервіс.
 */
class StudentServiceTest {

    private StudentRepository repository;
    private StudentService service;
    private Student student;

    @BeforeEach
    void setUp() {
        repository = mock(StudentRepository.class);
        service = new StudentService(repository, mock(StudyGroupRepository.class));
        student = student(1L, group(1L, "ІР-43"));
        when(repository.findById(1L)).thenReturn(Optional.of(student));
        when(repository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void changesStatusThroughState() {
        Student updated = service.changeStatus(1L, StudentStatus.ACADEMIC_LEAVE);

        assertEquals(StudentStatus.ACADEMIC_LEAVE, updated.getStatus());
        verify(repository).save(student);
    }

    @Test
    void invalidTransitionIsNotSaved() {
        student.setStatus(StudentStatus.GRADUATED);

        assertThrows(InvalidStatusTransitionException.class,
                () -> service.changeStatus(1L, StudentStatus.ACTIVE));
        verify(repository, never()).save(any(Student.class));
    }

    @Test
    void unknownStudentCausesNotFound() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getById(42L));
    }
}
