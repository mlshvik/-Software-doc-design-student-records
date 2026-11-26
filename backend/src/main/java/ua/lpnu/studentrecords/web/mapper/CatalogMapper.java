package ua.lpnu.studentrecords.web.mapper;

import ua.lpnu.studentrecords.domain.entity.Faculty;
import ua.lpnu.studentrecords.domain.entity.Specialty;
import ua.lpnu.studentrecords.domain.entity.StudyGroup;
import ua.lpnu.studentrecords.domain.entity.Subject;
import ua.lpnu.studentrecords.web.dto.FacultyResponse;
import ua.lpnu.studentrecords.web.dto.GroupResponse;
import ua.lpnu.studentrecords.web.dto.SpecialtyResponse;
import ua.lpnu.studentrecords.web.dto.SubjectResponse;

/**
 * Перетворення довідникових сутностей (факультет, спеціальність, група, дисципліна) у DTO.
 * <p>
 * Мапери викликаються всередині транзакції сервісу, тому ліниві зв'язки
 * (група → спеціальність → факультет) доступні без додаткових запитів у контролері.
 */
public final class CatalogMapper {

    private CatalogMapper() {
    }

    public static FacultyResponse toResponse(Faculty faculty) {
        return new FacultyResponse(faculty.getId(), faculty.getName(), faculty.getShortName());
    }

    public static SpecialtyResponse toResponse(Specialty specialty) {
        Faculty faculty = specialty.getFaculty();
        return new SpecialtyResponse(specialty.getId(), specialty.getCode(), specialty.getName(),
                faculty.getId(), faculty.getShortName());
    }

    public static GroupResponse toResponse(StudyGroup group) {
        Specialty specialty = group.getSpecialty();
        Faculty faculty = specialty.getFaculty();
        return new GroupResponse(group.getId(), group.getName(), group.getCourse(), group.getAdmissionYear(),
                specialty.getId(), specialty.getCode(), specialty.getName(),
                faculty.getId(), faculty.getShortName());
    }

    public static SubjectResponse toResponse(Subject subject) {
        return new SubjectResponse(subject.getId(), subject.getName(), subject.getCredits(),
                subject.getHours(), subject.getControlType());
    }
}
