package ua.lpnu.studentrecords.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.lpnu.studentrecords.domain.entity.Faculty;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {

    boolean existsByNameIgnoreCase(String name);
}
