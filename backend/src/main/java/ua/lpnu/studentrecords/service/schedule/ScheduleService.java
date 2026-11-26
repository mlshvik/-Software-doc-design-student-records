package ua.lpnu.studentrecords.service.schedule;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lpnu.studentrecords.domain.entity.ScheduleEntry;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.entity.TeachingAssignment;
import ua.lpnu.studentrecords.domain.enums.WeekType;
import ua.lpnu.studentrecords.domain.exception.NotFoundException;
import ua.lpnu.studentrecords.repository.ScheduleEntryRepository;
import ua.lpnu.studentrecords.repository.StudentRepository;
import ua.lpnu.studentrecords.repository.TeachingAssignmentRepository;
import ua.lpnu.studentrecords.web.dto.ScheduleConflictResponse;
import ua.lpnu.studentrecords.web.dto.ScheduleEntryRequest;
import ua.lpnu.studentrecords.web.dto.ScheduleEntryResponse;
import ua.lpnu.studentrecords.web.mapper.AcademicMapper;

import java.util.Comparator;
import java.util.List;

/**
 * Розклад занять: перегляд для групи, викладача і студента, додавання занять
 * з перевіркою конфліктів ({@link ScheduleConflictChecker}).
 */
@Service
@Transactional(readOnly = true)
public class ScheduleService {

    /** Порядок у сітці розкладу: день → пара → тип тижня. */
    public static final Comparator<ScheduleEntry> TIMETABLE_ORDER = Comparator
            .comparing(ScheduleEntry::getDayOfWeek)
            .thenComparing(ScheduleEntry::getPairNumber)
            .thenComparing(ScheduleEntry::getWeekType);

    private final ScheduleEntryRepository scheduleRepository;
    private final StudentRepository studentRepository;
    private final TeachingAssignmentRepository assignmentRepository;
    private final ScheduleConflictChecker conflictChecker;

    public ScheduleService(ScheduleEntryRepository scheduleRepository,
                           StudentRepository studentRepository,
                           TeachingAssignmentRepository assignmentRepository,
                           ScheduleConflictChecker conflictChecker) {
        this.scheduleRepository = scheduleRepository;
        this.studentRepository = studentRepository;
        this.assignmentRepository = assignmentRepository;
        this.conflictChecker = conflictChecker;
    }

    public List<ScheduleEntry> getGroupSchedule(Long groupId) {
        return sorted(scheduleRepository.findByGroupId(groupId));
    }

    public List<ScheduleEntry> getTeacherSchedule(Long teacherId) {
        return sorted(scheduleRepository.findByTeacherId(teacherId));
    }

    /**
     * Розклад студента – розклад його групи. Студент, який не відвідує заняття
     * (академвідпустка, відрахування, випуск – шаблон State), отримує порожній розклад.
     */
    public List<ScheduleEntry> getStudentSchedule(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Студент", studentId));
        if (!student.getState().canAttendClasses()) {
            return List.of();
        }
        return getGroupSchedule(student.getGroup().getId());
    }

    /** Конфлікти заняття з наявним розкладом (порожній список – конфліктів немає). */
    public List<ScheduleConflict> checkConflicts(ScheduleEntry candidate) {
        return conflictChecker.findConflicts(candidate,
                scheduleRepository.findAtTime(candidate.getDayOfWeek(), candidate.getPairNumber()));
    }

    /**
     * Додає заняття до розкладу.
     *
     * @throws ScheduleConflictException якщо викладач, аудиторія або група зайняті
     */
    @Transactional
    public ScheduleEntry create(ScheduleEntry entry) {
        List<ScheduleConflict> conflicts = checkConflicts(entry);
        if (!conflicts.isEmpty()) {
            throw new ScheduleConflictException(conflicts);
        }
        return scheduleRepository.save(entry);
    }

    @Transactional
    public void delete(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new NotFoundException("Заняття", id);
        }
        scheduleRepository.deleteById(id);
    }

    // ---- операції для REST API (результат – DTO, перетворення в межах транзакції) ----

    public List<ScheduleEntryResponse> groupTimetable(Long groupId) {
        return toResponses(getGroupSchedule(groupId));
    }

    public List<ScheduleEntryResponse> teacherTimetable(Long teacherId) {
        return toResponses(getTeacherSchedule(teacherId));
    }

    public List<ScheduleEntryResponse> studentTimetable(Long studentId) {
        return toResponses(getStudentSchedule(studentId));
    }

    /** Попередня перевірка заняття без збереження (для конструктора розкладу). */
    public List<ScheduleConflictResponse> check(ScheduleEntryRequest request, Long excludeEntryId) {
        ScheduleEntry candidate = toEntry(request);
        candidate.setId(excludeEntryId);
        return checkConflicts(candidate).stream().map(AcademicMapper::toResponse).toList();
    }

    @Transactional
    public ScheduleEntryResponse create(ScheduleEntryRequest request) {
        return AcademicMapper.toResponse(create(toEntry(request)));
    }

    /**
     * Змінює заняття. Перевірка конфліктів не враховує саме це заняття (порівняння за ідентифікатором).
     *
     * @throws ScheduleConflictException якщо новий час або аудиторія зайняті
     */
    @Transactional
    public ScheduleEntryResponse update(Long id, ScheduleEntryRequest request) {
        ScheduleEntry entry = scheduleRepository.findById(id).orElseThrow(() -> new NotFoundException("Заняття", id));
        ScheduleEntry candidate = toEntry(request);
        candidate.setId(id);
        List<ScheduleConflict> conflicts = checkConflicts(candidate);
        if (!conflicts.isEmpty()) {
            throw new ScheduleConflictException(conflicts);
        }
        entry.setAssignment(candidate.getAssignment());
        entry.setDayOfWeek(candidate.getDayOfWeek());
        entry.setPairNumber(candidate.getPairNumber());
        entry.setWeekType(candidate.getWeekType());
        entry.setClassroom(candidate.getClassroom());
        entry.setLessonType(candidate.getLessonType());
        return AcademicMapper.toResponse(entry);
    }

    private ScheduleEntry toEntry(ScheduleEntryRequest request) {
        TeachingAssignment assignment = assignmentRepository.findById(request.assignmentId())
                .orElseThrow(() -> new NotFoundException("Призначення", request.assignmentId()));
        WeekType weekType = request.weekType() != null ? request.weekType() : WeekType.ALL;
        return new ScheduleEntry(assignment, request.dayOfWeek(), request.pairNumber(), weekType,
                request.classroom().trim(), request.lessonType());
    }

    private static List<ScheduleEntryResponse> toResponses(List<ScheduleEntry> entries) {
        return entries.stream().map(AcademicMapper::toResponse).toList();
    }

    private static List<ScheduleEntry> sorted(List<ScheduleEntry> entries) {
        return entries.stream().sorted(TIMETABLE_ORDER).toList();
    }
}
