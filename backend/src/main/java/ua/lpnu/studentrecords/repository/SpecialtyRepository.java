package ua.lpnu.studentrecords.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.lpnu.studentrecords.domain.entity.Specialty;

import java.util.List;

public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

    List<Specialty> findByFacultyIdOrderByCode(Long facultyId);
}
