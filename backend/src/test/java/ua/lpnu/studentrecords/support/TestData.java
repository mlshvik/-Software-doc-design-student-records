package ua.lpnu.studentrecords.support;

import ua.lpnu.studentrecords.domain.entity.AppUser;
import ua.lpnu.studentrecords.domain.entity.Faculty;
import ua.lpnu.studentrecords.domain.entity.Grade;
import ua.lpnu.studentrecords.domain.entity.ScheduleEntry;
import ua.lpnu.studentrecords.domain.entity.Specialty;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.entity.StudyGroup;
import ua.lpnu.studentrecords.domain.entity.Subject;
import ua.lpnu.studentrecords.domain.entity.Teacher;
import ua.lpnu.studentrecords.domain.entity.TeachingAssignment;
import ua.lpnu.studentrecords.domain.enums.ControlType;
import ua.lpnu.studentrecords.domain.enums.GradeAttempt;
import ua.lpnu.studentrecords.domain.enums.LessonType;
import ua.lpnu.studentrecords.domain.enums.Role;
import ua.lpnu.studentrecords.domain.enums.WeekType;

import java.math.BigDecimal;
import java.time.DayOfWeek;

/**
 * Фабрика тестових об'єктів доменної моделі з ідентифікаторами (без БД).
 */
public final class TestData {

    private TestData() {
    }

    public static StudyGroup group(long id, String name) {
        Faculty faculty = new Faculty("Інститут комп'ютерних наук та інформаційних технологій", "ІКНІ");
        faculty.setId(1L);
        Specialty specialty = new Specialty("121", "Інженерія програмного забезпечення", faculty);
        specialty.setId(1L);
        StudyGroup group = new StudyGroup(name, specialty, 4, 2023);
        group.setId(id);
        return group;
    }

    public static Teacher teacher(long id, String lastName) {
        Teacher teacher = new Teacher(lastName, "Олена", "Іванівна", "доцент", "Кафедра ПЗ");
        teacher.setId(id);
        return teacher;
    }

    public static Subject subject(long id, String name, String credits, ControlType controlType) {
        Subject subject = new Subject(name, new BigDecimal(credits), 90, controlType);
        subject.setId(id);
        return subject;
    }

    public static TeachingAssignment assignment(long id, Subject subject, Teacher teacher, StudyGroup group) {
        TeachingAssignment assignment = new TeachingAssignment(subject, teacher, group, 7, "2026/2027");
        assignment.setId(id);
        return assignment;
    }

    public static Student student(long id, StudyGroup group) {
        Student student = new Student("Маліш", "Вікторія", "Олегівна", "IR23" + id, group);
        student.setId(id);
        return student;
    }

    public static Student studentWithUser(long id, StudyGroup group) {
        Student student = student(id, group);
        AppUser user = new AppUser("student" + id, "hash", Role.STUDENT);
        user.setId(100 + id);
        student.setUser(user);
        return student;
    }

    public static Grade grade(Student student, TeachingAssignment assignment, int score, GradeAttempt attempt) {
        return new Grade(student, assignment, score, attempt, assignment.getTeacher());
    }

    public static ScheduleEntry entry(long id, TeachingAssignment assignment, DayOfWeek day, int pair,
                                      WeekType weekType, String classroom, LessonType lessonType) {
        ScheduleEntry entry = new ScheduleEntry(assignment, day, pair, weekType, classroom, lessonType);
        entry.setId(id);
        return entry;
    }
}
