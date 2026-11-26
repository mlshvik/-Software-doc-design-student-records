package ua.lpnu.studentrecords.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.lpnu.studentrecords.domain.entity.StudyGroup;

import java.util.List;
import java.util.Optional;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {

    Optional<StudyGroup> findByName(String name);

    List<StudyGroup> findBySpecialtyIdOrderByName(Long specialtyId);

    List<StudyGroup> findByCourseOrderByName(Integer course);

    List<StudyGroup> findAllByOrderByCourseAscNameAsc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
