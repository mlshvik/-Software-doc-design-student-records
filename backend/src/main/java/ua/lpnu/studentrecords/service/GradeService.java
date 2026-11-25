package ua.lpnu.studentrecords.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lpnu.studentrecords.domain.entity.Grade;
import ua.lpnu.studentrecords.domain.entity.Student;
import ua.lpnu.studentrecords.domain.entity.Teacher;
import ua.lpnu.studentrecords.domain.entity.TeachingAssignment;
import ua.lpnu.studentrecords.domain.enums.GradeAttempt;
import ua.lpnu.studentrecords.domain.exception.NotFoundException;
import ua.lpnu.studentrecords.pattern.chain.AttemptLimitHandler;
import ua.lpnu.studentrecords.pattern.chain.GradeValidationChainBuilder;
import ua.lpnu.studentrecords.pattern.chain.GradeValidationContext;
import ua.lpnu.studentrecords.pattern.observer.GradeEventPublisher;
import ua.lpnu.studentrecords.pattern.observer.GradeRecordedEvent;
import ua.lpnu.studentrecords.pattern.scale.GradingScale;
import ua.lpnu.studentrecords.pattern.strategy.FinalGradeSelector;
import ua.lpnu.studentrecords.repository.GradeRepository;
import ua.lpnu.studentrecords.repository.StudentRepository;
import ua.lpnu.studentrecords.repository.TeacherRepository;
import ua.lpnu.studentrecords.repository.TeachingAssignmentRepository;
import ua.lpnu.studentrecords.web.dto.GradeRequest;
import ua.lpnu.studentrecords.web.dto.GradeResponse;
import ua.lpnu.studentrecords.web.dto.GradeSheetResponse;
import ua.lpnu.studentrecords.web.dto.GradeSheetRow;
import ua.lpnu.studentrecords.web.mapper.AcademicMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Виставлення і перегляд оцінок.
 * <p>
 * Сценарій {@link #recordGrade(RecordGradeCommand)} поєднує кілька шаблонів:
 * <ol>
 *     <li>запит перевіряє ланцюжок обробників (<b>Chain of Responsibility</b>),
 *         один з яких питає стан студента (<b>State</b>);</li>
 *     <li>оцінка зберігається;</li>
 *     <li>{@link GradeEventPublisher} сповіщає спостерігачів (<b>Observer</b>): борги, сповіщення,
 *         середній бал (<b>Strategy</b>).</li>
 * </ol>
 * Сервіс не знає ні конкретних правил валідації, ні конкретних реакцій на нову оцінку
 * (низька зв'язаність, GRASP Low Coupling / Controller).
 */
@Service
@Transactional(readOnly = true)
public class GradeService {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final TeachingAssignmentRepository assignmentRepository;
    private final TeacherRepository teacherRepository;
    private final GradeValidationChainBuilder chainBuilder;
    private final GradeEventPublisher eventPublisher;

    public GradeService(GradeRepository gradeRepository,
                        StudentRepository studentRepository,
                        TeachingAssignmentRepository assignmentRepository,
                        TeacherRepository teacherRepository,
                        GradeValidationChainBuilder chainBuilder,
                        GradeEventPublisher eventPublisher) {
        this.gradeRepository = gradeRepository;
        this.studentRepository = studentRepository;
        this.assignmentRepository = assignmentRepository;
        this.teacherRepository = teacherRepository;
        this.chainBuilder = chainBuilder;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Виставляє оцінку після перевірки всіх правил і сповіщає спостерігачів.
     *
     * @throws NotFoundException якщо студента, призначення чи викладача не знайдено
     * @throws ua.lpnu.studentrecords.domain.exception.GradeValidationException якщо оцінку відхилено
     */
    @Transactional
    public Grade recordGrade(RecordGradeCommand command) {
        Student student = studentRepository.findById(command.studentId())
                .orElseThrow(() -> new NotFoundException("Студент", command.studentId()));
        TeachingAssignment assignment = assignmentRepository.findById(command.assignmentId())
                .orElseThrow(() -> new NotFoundException("Призначення", command.assignmentId()));
        Teacher grader = command.teacherId() == null ? null
                : teacherRepository.findById(command.teacherId())
                        .orElseThrow(() -> new NotFoundException("Викладач", command.teacherId()));

        List<Grade> previous = gradeRepository
                .findByStudentIdAndAssignmentIdOrderByGradedAtAsc(student.getId(), assignment.getId());
        GradeAttempt attempt = command.attempt() != null
                ? command.attempt()
                : AttemptLimitHandler.expectedAttempt(previous).orElse(GradeAttempt.REGULAR);

        chainBuilder.build().handle(
                new GradeValidationContext(student, assignment, grader, command.score(), attempt, previous));

        Teacher gradedBy = grader != null ? grader : assignment.getTeacher();
        Grade saved = gradeRepository.save(new Grade(student, assignment, command.score(), attempt, gradedBy));
        eventPublisher.notifyListeners(new GradeRecordedEvent(saved, student));
        return saved;
    }

    /** Усі оцінки студента (з перескладаннями) у хронологічному порядку. */
    public List<Grade> getStudentGrades(Long studentId) {
        return gradeRepository.findByStudentIdOrderByGradedAtAsc(studentId);
    }

    /** Оцінки за призначенням – основа відомості групи. */
    public List<Grade> getAssignmentGrades(Long assignmentId) {
        return gradeRepository.findByAssignmentId(assignmentId);
    }

    /** Наступна допустима спроба студента з дисципліни (порожньо – складено або спроби вичерпано). */
    public Optional<GradeAttempt> nextAttempt(Long studentId, Long assignmentId) {
        return AttemptLimitHandler.expectedAttempt(
                gradeRepository.findByStudentIdAndAssignmentIdOrderByGradedAtAsc(studentId, assignmentId));
    }

    // ---- операції для REST API (результат – DTO, перетворення в межах транзакції) ----

    /**
     * Виставлення оцінки з REST API.
     *
     * @param teacherId викладач, що виставляє оцінку; {@code null} – адміністратор (оцінку
     *                  буде записано від імені викладача дисципліни)
     */
    @Transactional
    public GradeResponse record(GradeRequest request, Long teacherId) {
        Grade grade = recordGrade(new RecordGradeCommand(request.studentId(), request.assignmentId(),
                request.score(), request.attempt(), teacherId));
        return AcademicMapper.toResponse(grade);
    }

    public List<GradeResponse> studentGrades(Long studentId) {
        return getStudentGrades(studentId).stream().map(AcademicMapper::toResponse).toList();
    }

    /**
     * Відомість групи з дисципліни: усі студенти групи, їхні спроби, підсумкова оцінка
     * і наступна допустима спроба (визначається тим самим правилом, що й у ланцюжку валідації).
     */
    public GradeSheetResponse getGradeSheet(Long assignmentId) {
        TeachingAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Призначення", assignmentId));
        Map<Long, List<Grade>> gradesByStudent = new LinkedHashMap<>();
        gradeRepository.findByAssignmentId(assignmentId).stream()
                .sorted(Comparator.comparing(Grade::getGradedAt))
                .forEach(g -> gradesByStudent.computeIfAbsent(g.getStudent().getId(), id -> new ArrayList<>()).add(g));
        List<GradeSheetRow> rows = new ArrayList<>();
        for (Student student : studentRepository.findByGroupIdOrderByLastNameAscFirstNameAsc(
                assignment.getGroup().getId())) {
            List<Grade> grades = gradesByStudent.getOrDefault(student.getId(), List.of());
            Grade finalGrade = FinalGradeSelector.selectFinal(grades).stream().findFirst().orElse(null);
            GradeAttempt next = student.canBeGraded()
                    ? AttemptLimitHandler.expectedAttempt(grades).orElse(null)
                    : null;
            rows.add(new GradeSheetRow(
                    student.getId(),
                    student.getFullName(),
                    student.getRecordBookNumber(),
                    student.getStatus(),
                    student.canBeGraded(),
                    grades.stream().map(AcademicMapper::toResponse).toList(),
                    finalGrade == null ? null : finalGrade.getScore(),
                    finalGrade == null ? null : GradingScale.INSTANCE.toEcts(finalGrade.getScore()),
                    next));
        }
        return new GradeSheetResponse(AcademicMapper.toResponse(assignment), rows);
    }
}
