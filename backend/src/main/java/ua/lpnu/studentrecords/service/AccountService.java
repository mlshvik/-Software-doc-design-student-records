package ua.lpnu.studentrecords.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lpnu.studentrecords.domain.entity.AppUser;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.entity.Teacher;
import ua.lpnu.studentrecords.domain.enums.Role;
import ua.lpnu.studentrecords.domain.exception.NotFoundException;
import ua.lpnu.studentrecords.repository.AppUserRepository;
import ua.lpnu.studentrecords.repository.StudentRepository;
import ua.lpnu.studentrecords.repository.TeacherRepository;
import ua.lpnu.studentrecords.web.dto.CurrentUserResponse;

import java.util.Optional;

/**
 * Відомості про обліковий запис: роль і пов'язаний студент чи викладач.
 */
@Service
@Transactional(readOnly = true)
public class AccountService {

    private final AppUserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    public AccountService(AppUserRepository userRepository,
                          StudentRepository studentRepository,
                          TeacherRepository teacherRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
    }

    public CurrentUserResponse describe(Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Користувач", userId));
        Optional<Student> student = studentRepository.findByUserId(userId);
        Optional<Teacher> teacher = teacherRepository.findByUserId(userId);
        String fullName = student.map(Student::getFullName)
                .or(() -> teacher.map(Teacher::getFullName))
                .orElse(user.getRole() == Role.ADMIN ? "Адміністратор" : user.getUsername());
        return new CurrentUserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                fullName,
                student.map(Student::getId).orElse(null),
                teacher.map(Teacher::getId).orElse(null));
    }

    public Optional<Long> findStudentId(Long userId) {
        return studentRepository.findByUserId(userId).map(Student::getId);
    }

    public Optional<Long> findTeacherId(Long userId) {
        return teacherRepository.findByUserId(userId).map(Teacher::getId);
    }
}
