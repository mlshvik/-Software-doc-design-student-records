package ua.lpnu.studentrecords.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.lpnu.studentrecords.domain.entity.Subject;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findAllByOrderByNameAsc();
}
