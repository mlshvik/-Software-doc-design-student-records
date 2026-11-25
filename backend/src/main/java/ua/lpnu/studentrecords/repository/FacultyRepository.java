package ua.lpnu.studentrecords.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.lpnu.studentrecords.domain.entity.Faculty;

import java.util.List;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Faculty> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
