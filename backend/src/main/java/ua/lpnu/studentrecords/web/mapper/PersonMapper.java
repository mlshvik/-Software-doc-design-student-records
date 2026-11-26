package ua.lpnu.studentrecords.web.mapper;

import ua.lpnu.studentrecords.domain.entity.AppUser;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.entity.StudyGroup;
import ua.lpnu.studentrecords.domain.entity.Teacher;
import ua.lpnu.studentrecords.web.dto.StudentRequest;
import ua.lpnu.studentrecords.web.dto.StudentResponse;
import ua.lpnu.studentrecords.web.dto.TeacherRequest;
import ua.lpnu.studentrecords.web.dto.TeacherResponse;

/**
 * Перетворення студентів і викладачів у DTO та перенесення даних із запитів у сутності.
 */
public final class PersonMapper {

    private PersonMapper() {
    }

    public static StudentResponse toResponse(Student student) {
        StudyGroup group = student.getGroup();
        return new StudentResponse(
                student.getId(),
                student.getLastName(),
                student.getFirstName(),
                student.getMiddleName(),
                student.getFullName(),
                student.getRecordBookNumber(),
                student.getEmail(),
                student.getPhone(),
                student.getBirthDate(),
                group.getId(),
                group.getName(),
                group.getCourse(),
                student.getStatus(),
                student.getState().title(),
                usernameOf(student.getUser()));
    }

    public static TeacherResponse toResponse(Teacher teacher) {
        return new TeacherResponse(
                teacher.getId(),
                teacher.getLastName(),
                teacher.getFirstName(),
                teacher.getMiddleName(),
                teacher.getFullName(),
                teacher.getShortName(),
                teacher.getPosition(),
                teacher.getAcademicDegree(),
                teacher.getDepartment(),
                teacher.getEmail(),
                usernameOf(teacher.getUser()));
    }

    /** Переносить редаговані поля студента (крім групи і статусу). */
    public static void apply(StudentRequest request, Student student) {
        student.setLastName(request.lastName().trim());
        student.setFirstName(request.firstName().trim());
        student.setMiddleName(blankToNull(request.middleName()));
        student.setRecordBookNumber(request.recordBookNumber().trim());
        student.setEmail(blankToNull(request.email()));
        student.setPhone(blankToNull(request.phone()));
        student.setBirthDate(request.birthDate());
    }

    public static void apply(TeacherRequest request, Teacher teacher) {
        teacher.setLastName(request.lastName().trim());
        teacher.setFirstName(request.firstName().trim());
        teacher.setMiddleName(blankToNull(request.middleName()));
        teacher.setPosition(blankToNull(request.position()));
        teacher.setAcademicDegree(blankToNull(request.academicDegree()));
        teacher.setDepartment(blankToNull(request.department()));
        teacher.setEmail(blankToNull(request.email()));
    }

    static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String usernameOf(AppUser user) {
        return user == null ? null : user.getUsername();
    }
}
