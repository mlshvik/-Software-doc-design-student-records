package ua.lpnu.studentrecords.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.enums.StudentStatus;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторій студентів. {@link JpaSpecificationExecutor} використовується
 * для динамічної фільтрації (факультет, спеціальність, група, курс, статус, пошук за ПІБ).
 */
public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

    List<Student> findByGroupIdOrderByLastNameAscFirstNameAsc(Long groupId);

    List<Student> findByGroupIdAndStatus(Long groupId, StudentStatus status);

    Optional<Student> findByUserId(Long userId);

    Optional<Student> findByRecordBookNumber(String recordBookNumber);

    boolean existsByRecordBookNumber(String recordBookNumber);

    long countByStatus(StudentStatus status);

    boolean existsByRecordBookNumberAndIdNot(String recordBookNumber, Long id);
}
