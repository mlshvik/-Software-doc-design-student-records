package ua.lpnu.studentrecords.repository;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ua.lpnu.studentrecords.domain.entity.Specialty;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.entity.StudyGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Динамічні умови пошуку студентів (JPA Criteria API) для {@link StudentRepository}.
 */
public final class StudentSpecifications {

    private StudentSpecifications() {
    }

    public static Specification<Student> matching(StudentSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Student, StudyGroup> group = root.join("group");
            if (criteria.groupId() != null) {
                predicates.add(cb.equal(group.get("id"), criteria.groupId()));
            }
            if (criteria.course() != null) {
                predicates.add(cb.equal(group.get("course"), criteria.course()));
            }
            if (criteria.specialtyId() != null || criteria.facultyId() != null) {
                Join<StudyGroup, Specialty> specialty = group.join("specialty");
                if (criteria.specialtyId() != null) {
                    predicates.add(cb.equal(specialty.get("id"), criteria.specialtyId()));
                }
                if (criteria.facultyId() != null) {
                    predicates.add(cb.equal(specialty.get("faculty").get("id"), criteria.facultyId()));
                }
            }
            if (criteria.status() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.status()));
            }
            if (criteria.query() != null && !criteria.query().isBlank()) {
                String pattern = "%" + criteria.query().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.<String>get("lastName")), pattern),
                        cb.like(cb.lower(root.<String>get("firstName")), pattern),
                        cb.like(cb.lower(root.<String>get("recordBookNumber")), pattern)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
