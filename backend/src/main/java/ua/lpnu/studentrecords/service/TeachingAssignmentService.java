package ua.lpnu.studentrecords.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lpnu.studentrecords.domain.entity.TeachingAssignment;
import ua.lpnu.studentrecords.domain.exception.NotFoundException;
import ua.lpnu.studentrecords.repository.TeachingAssignmentRepository;
import ua.lpnu.studentrecords.web.dto.AssignmentRequest;
import ua.lpnu.studentrecords.web.dto.AssignmentResponse;
import ua.lpnu.studentrecords.web.mapper.AcademicMapper;

import java.util.Comparator;
import java.util.List;

/**
 * Призначення викладачів на дисципліни в групах (навчальне навантаження).
 */
@Service
@Transactional(readOnly = true)
public class TeachingAssignmentService {

    private static final Comparator<TeachingAssignment> ORDER = Comparator
            .comparing(TeachingAssignment::getAcademicYear).reversed()
            .thenComparing(TeachingAssignment::getSemester)
            .thenComparing(a -> a.getGroup().getName())
            .thenComparing(a -> a.getSubject().getName());

    private final TeachingAssignmentRepository assignmentRepository;
    private final SubjectService subjectService;
    private final TeacherService teacherService;
    private final StudyGroupService groupService;

    public TeachingAssignmentService(TeachingAssignmentRepository assignmentRepository,
                                     SubjectService subjectService,
                                     TeacherService teacherService,
                                     StudyGroupService groupService) {
        this.assignmentRepository = assignmentRepository;
        this.subjectService = subjectService;
        this.teacherService = teacherService;
        this.groupService = groupService;
    }

    /** Призначення з необов'язковими фільтрами за викладачем, групою, навчальним роком і семестром. */
    public List<AssignmentResponse> find(Long teacherId, Long groupId, String academicYear, Integer semester) {
        List<TeachingAssignment> source;
        if (teacherId != null) {
            source = assignmentRepository.findByTeacherId(teacherId);
        } else if (groupId != null) {
            source = assignmentRepository.findByGroupId(groupId);
        } else {
            source = assignmentRepository.findAllWithDetails();
        }
        return source.stream()
                .filter(a -> groupId == null || groupId.equals(a.getGroup().getId()))
                .filter(a -> academicYear == null || academicYear.equals(a.getAcademicYear()))
                .filter(a -> semester == null || semester.equals(a.getSemester()))
                .sorted(ORDER)
                .map(AcademicMapper::toResponse)
                .toList();
    }

    public AssignmentResponse get(Long id) {
        return AcademicMapper.toResponse(getEntity(id));
    }

    TeachingAssignment getEntity(Long id) {
        return assignmentRepository.findById(id).orElseThrow(() -> new NotFoundException("Призначення", id));
    }

    /** Чи веде викладач цю дисципліну (для перевірки прав доступу). */
    public boolean isOwnedBy(Long assignmentId, Long teacherId) {
        return assignmentId != null && teacherId != null
                && assignmentRepository.existsByIdAndTeacherId(assignmentId, teacherId);
    }

    @Transactional
    public AssignmentResponse create(AssignmentRequest request) {
        TeachingAssignment assignment = new TeachingAssignment(
                subjectService.getEntity(request.subjectId()),
                teacherService.getEntity(request.teacherId()),
                groupService.getEntity(request.groupId()),
                request.semester(),
                request.academicYear().trim());
        TeachingAssignment saved = assignmentRepository.save(assignment);
        assignmentRepository.flush();
        return AcademicMapper.toResponse(saved);
    }

    @Transactional
    public AssignmentResponse update(Long id, AssignmentRequest request) {
        TeachingAssignment assignment = getEntity(id);
        assignment.setSubject(subjectService.getEntity(request.subjectId()));
        assignment.setTeacher(teacherService.getEntity(request.teacherId()));
        assignment.setGroup(groupService.getEntity(request.groupId()));
        assignment.setSemester(request.semester());
        assignment.setAcademicYear(request.academicYear().trim());
        assignmentRepository.flush();
        return AcademicMapper.toResponse(assignment);
    }

    @Transactional
    public void delete(Long id) {
        assignmentRepository.delete(getEntity(id));
        assignmentRepository.flush();
    }
}
