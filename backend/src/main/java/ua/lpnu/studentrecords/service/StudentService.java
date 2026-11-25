package ua.lpnu.studentrecords.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.enums.StudentStatus;
import ua.lpnu.studentrecords.domain.exception.NotFoundException;
import ua.lpnu.studentrecords.repository.StudentRepository;
import ua.lpnu.studentrecords.repository.StudentSearchCriteria;
import ua.lpnu.studentrecords.repository.StudentSpecifications;

import java.util.List;

/**
 * Пошук студентів і керування їхнім статусом.
 */
@Service
@Transactional(readOnly = true)
public class StudentService {

    private static final Sort BY_NAME = Sort.by("lastName", "firstName", "middleName");

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student getById(Long id) {
        return studentRepository.findById(id).orElseThrow(() -> new NotFoundException("Студент", id));
    }

    /** Студент, пов'язаний з обліковим записом (для кабінету студента). */
    public Student getByUserId(Long userId) {
        return studentRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Студент для користувача", userId));
    }

    /** Пошук за факультетом, спеціальністю, групою, курсом, статусом і фрагментом ПІБ. */
    public List<Student> search(StudentSearchCriteria criteria) {
        StudentSearchCriteria effective = criteria == null ? StudentSearchCriteria.empty() : criteria;
        return studentRepository.findAll(StudentSpecifications.matching(effective), BY_NAME);
    }

    public List<Student> getGroupStudents(Long groupId) {
        return studentRepository.findByGroupIdOrderByLastNameAscFirstNameAsc(groupId);
    }

    /**
     * Змінює статус студента. Допустимість переходу перевіряє поточний стан (шаблон State).
     *
     * @throws ua.lpnu.studentrecords.domain.exception.InvalidStatusTransitionException
     *         якщо перехід заборонено
     */
    @Transactional
    public Student changeStatus(Long studentId, StudentStatus target) {
        Student student = getById(studentId);
        student.changeStatus(target);
        return studentRepository.save(student);
    }
}
