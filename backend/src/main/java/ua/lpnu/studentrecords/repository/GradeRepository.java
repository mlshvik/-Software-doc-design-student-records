package ua.lpnu.studentrecords.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.lpnu.studentrecords.domain.entity.Grade;

import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    @EntityGraph(attributePaths = {"assignment", "assignment.subject", "assignment.teacher"})
    List<Grade> findByStudentIdOrderByGradedAtAsc(Long studentId);

    List<Grade> findByStudentIdAndAssignmentIdOrderByGradedAtAsc(Long studentId, Long assignmentId);

    @EntityGraph(attributePaths = {"student"})
    List<Grade> findByAssignmentId(Long assignmentId);

    /** Усі оцінки, виставлені за дисципліни конкретного викладача (для статистики по викладачах). */
    @Query("select g from Grade g join fetch g.assignment a join fetch a.subject " +
           "where a.teacher.id = :teacherId")
    List<Grade> findByTeacherId(@Param("teacherId") Long teacherId);

    /** Усі оцінки студентів групи (для звіту про успішність). */
    @Query("select g from Grade g join fetch g.student s join fetch g.assignment a join fetch a.subject " +
           "where s.group.id = :groupId")
    List<Grade> findByGroupId(@Param("groupId") Long groupId);

    /**
     * Усі оцінки разом з даними для звітів (студент, дисципліна, викладач, група, спеціальність,
     * факультет) одним SQL-запитом – без проблеми N+1.
     */
    @Query("select g from Grade g join fetch g.student s join fetch g.assignment a join fetch a.subject " +
           "join fetch a.teacher join fetch a.group gr join fetch gr.specialty sp join fetch sp.faculty")
    List<Grade> findAllForReports();
}
