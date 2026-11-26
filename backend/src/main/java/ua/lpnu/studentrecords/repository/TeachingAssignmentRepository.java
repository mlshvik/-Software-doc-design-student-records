package ua.lpnu.studentrecords.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ua.lpnu.studentrecords.domain.entity.TeachingAssignment;

import java.util.List;

public interface TeachingAssignmentRepository extends JpaRepository<TeachingAssignment, Long> {

    @EntityGraph(attributePaths = {"subject", "teacher", "group"})
    List<TeachingAssignment> findByTeacherId(Long teacherId);

    @EntityGraph(attributePaths = {"subject", "teacher", "group"})
    List<TeachingAssignment> findByGroupId(Long groupId);

    @EntityGraph(attributePaths = {"subject", "teacher", "group"})
    List<TeachingAssignment> findByGroupIdAndSemester(Long groupId, Integer semester);

    boolean existsByIdAndTeacherId(Long id, Long teacherId);

    /** Усі призначення з дисципліною, викладачем і групою. */
    @EntityGraph(attributePaths = {"subject", "teacher", "group"})
    @Query("select a from TeachingAssignment a")
    List<TeachingAssignment> findAllWithDetails();
}
