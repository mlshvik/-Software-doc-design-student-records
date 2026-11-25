package ua.lpnu.studentrecords.service.schedule;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lpnu.studentrecords.domain.entity.ScheduleEntry;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.exception.NotFoundException;
import ua.lpnu.studentrecords.repository.ScheduleEntryRepository;
import ua.lpnu.studentrecords.repository.StudentRepository;

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
    private final ScheduleConflictChecker conflictChecker;

    public ScheduleService(ScheduleEntryRepository scheduleRepository,
                           StudentRepository studentRepository,
                           ScheduleConflictChecker conflictChecker) {
        this.scheduleRepository = scheduleRepository;
        this.studentRepository = studentRepository;
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

    private static List<ScheduleEntry> sorted(List<ScheduleEntry> entries) {
        return entries.stream().sorted(TIMETABLE_ORDER).toList();
    }
}
