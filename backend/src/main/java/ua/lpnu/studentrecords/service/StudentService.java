package ua.lpnu.studentrecords.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.entity.StudyGroup;
import ua.lpnu.studentrecords.domain.enums.StudentStatus;
import ua.lpnu.studentrecords.domain.exception.ConflictException;
import ua.lpnu.studentrecords.domain.exception.NotFoundException;
import ua.lpnu.studentrecords.repository.StudentRepository;
import ua.lpnu.studentrecords.repository.StudentSearchCriteria;
import ua.lpnu.studentrecords.repository.StudentSpecifications;
import ua.lpnu.studentrecords.repository.StudyGroupRepository;
import ua.lpnu.studentrecords.web.dto.StudentRequest;
import ua.lpnu.studentrecords.web.dto.StudentResponse;
import ua.lpnu.studentrecords.web.mapper.PersonMapper;

import java.util.List;

/**
 * Облік студентів: пошук, створення і редагування, керування статусом.
 * <p>
 * Методи, що повертають DTO ({@link StudentResponse}), виконують перетворення всередині транзакції,
 * тому контролер не звертається до лінивих зв'язків сутностей.
 */
@Service
@Transactional(readOnly = true)
public class StudentService {

    private static final Sort BY_NAME = Sort.by("lastName", "firstName", "middleName");

    private final StudentRepository studentRepository;
    private final StudyGroupRepository groupRepository;

    public StudentService(StudentRepository studentRepository, StudyGroupRepository groupRepository) {
        this.studentRepository = studentRepository;
        this.groupRepository = groupRepository;
    }

    public Student getById(Long id) {
        return studentRepository.findById(id).orElseThrow(() -> new NotFoundException("Студент", id));
    }

    /** Студент, пов'язаний з обліковим записом (для кабінету студента). */
    public Student getByUserId(Long userId) {
        return studentRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Студент для користувача", userId));
    }

    /** Пошук за факультетом, спеціальністю, групою, курсом, статусом і фрагментом ПІБ. */
    public List<Student> search(StudentSearchCriteria criteria) {
        StudentSearchCriteria effective = criteria == null ? StudentSearchCriteria.empty() : criteria;
        return studentRepository.findAll(StudentSpecifications.matching(effective), BY_NAME);
    }

    public List<Student> getGroupStudents(Long groupId) {
        return studentRepository.findByGroupIdOrderByLastNameAscFirstNameAsc(groupId);
    }

    /**
     * Змінює статус студента. Допустимість переходу перевіряє поточний стан (шаблон State).
     *
     * @throws ua.lpnu.studentrecords.domain.exception.InvalidStatusTransitionException
     *         якщо перехід заборонено
     */
    @Transactional
    public Student changeStatus(Long studentId, StudentStatus target) {
        Student student = getById(studentId);
        student.changeStatus(target);
        return studentRepository.save(student);
    }

    /** Пошук для REST API: результат одразу перетворено на DTO. */
    public List<StudentResponse> searchStudents(StudentSearchCriteria criteria) {
        return search(criteria).stream().map(PersonMapper::toResponse).toList();
    }

    public StudentResponse getStudent(Long id) {
        return PersonMapper.toResponse(getById(id));
    }

    /** Новий студент завжди отримує статус «навчається». */
    @Transactional
    public StudentResponse create(StudentRequest request) {
        String recordBook = request.recordBookNumber().trim();
        if (studentRepository.existsByRecordBookNumber(recordBook)) {
            throw new ConflictException("Студент із заліковою книжкою " + recordBook + " уже існує");
        }
        Student student = new Student(request.lastName().trim(), request.firstName().trim(), null,
                recordBook, findGroup(request.groupId()));
        PersonMapper.apply(request, student);
        return PersonMapper.toResponse(studentRepository.save(student));
    }

    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = getById(id);
        String recordBook = request.recordBookNumber().trim();
        if (studentRepository.existsByRecordBookNumberAndIdNot(recordBook, id)) {
            throw new ConflictException("Студент із заліковою книжкою " + recordBook + " уже існує");
        }
        PersonMapper.apply(request, student);
        student.setGroup(findGroup(request.groupId()));
        return PersonMapper.toResponse(student);
    }

    /** Зміна статусу для REST API (перевірка переходу – шаблон State). */
    @Transactional
    public StudentResponse updateStatus(Long id, StudentStatus target) {
        return PersonMapper.toResponse(changeStatus(id, target));
    }

    /** Видалення; студента з оцінками БД не дозволить видалити (замість цього – статус «відрахований»). */
    @Transactional
    public void delete(Long id) {
        studentRepository.delete(getById(id));
        studentRepository.flush();
    }

    private StudyGroup findGroup(Long groupId) {
        return groupRepository.findById(groupId).orElseThrow(() -> new NotFoundException("Група", groupId));
    }
}
