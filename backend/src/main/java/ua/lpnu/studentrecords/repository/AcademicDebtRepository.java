package ua.lpnu.studentrecords.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ua.lpnu.studentrecords.domain.entity.AcademicDebt;
import ua.lpnu.studentrecords.domain.enums.DebtStatus;

import java.util.List;
import java.util.Optional;

public interface AcademicDebtRepository extends JpaRepository<AcademicDebt, Long> {

    @EntityGraph(attributePaths = {"assignment", "assignment.subject", "assignment.teacher"})
    List<AcademicDebt> findByStudentIdOrderByOpenedAtDesc(Long studentId);

    @EntityGraph(attributePaths = {"student", "student.group", "assignment", "assignment.subject", "assignment.teacher"})
    List<AcademicDebt> findByStatus(DebtStatus status);

    Optional<AcademicDebt> findFirstByStudentIdAndAssignmentIdAndStatus(Long studentId, Long assignmentId,
                                                                       DebtStatus status);

    long countByStudentIdAndStatus(Long studentId, DebtStatus status);

    /** Усі заборгованості з даними студента, групи і дисципліни (для звітів і фільтрів). */
    @EntityGraph(attributePaths = {"student", "student.group", "student.group.specialty",
            "student.group.specialty.faculty", "assignment", "assignment.subject", "assignment.teacher"})
    @Query("select d from AcademicDebt d")
    List<AcademicDebt> findAllForReports();
}
