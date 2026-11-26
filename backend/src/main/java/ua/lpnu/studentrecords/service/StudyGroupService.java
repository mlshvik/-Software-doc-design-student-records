package ua.lpnu.studentrecords.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lpnu.studentrecords.domain.entity.StudyGroup;
import ua.lpnu.studentrecords.domain.exception.ConflictException;
import ua.lpnu.studentrecords.domain.exception.NotFoundException;
import ua.lpnu.studentrecords.repository.StudyGroupRepository;
import ua.lpnu.studentrecords.web.dto.GroupRequest;
import ua.lpnu.studentrecords.web.dto.GroupResponse;
import ua.lpnu.studentrecords.web.mapper.CatalogMapper;

import java.util.List;

/**
 * Довідник академічних груп.
 */
@Service
@Transactional(readOnly = true)
public class StudyGroupService {

    private final StudyGroupRepository groupRepository;
    private final SpecialtyService specialtyService;

    public StudyGroupService(StudyGroupRepository groupRepository, SpecialtyService specialtyService) {
        this.groupRepository = groupRepository;
        this.specialtyService = specialtyService;
    }

    /** Групи, відсортовані за курсом і назвою; фільтри необов'язкові. */
    public List<GroupResponse> findAll(Long facultyId, Long specialtyId, Integer course) {
        return groupRepository.findAllByOrderByCourseAscNameAsc().stream()
                .filter(g -> specialtyId == null || specialtyId.equals(g.getSpecialty().getId()))
                .filter(g -> facultyId == null || facultyId.equals(g.getSpecialty().getFaculty().getId()))
                .filter(g -> course == null || course.equals(g.getCourse()))
                .map(CatalogMapper::toResponse)
                .toList();
    }

    public GroupResponse get(Long id) {
        return CatalogMapper.toResponse(getEntity(id));
    }

    StudyGroup getEntity(Long id) {
        return groupRepository.findById(id).orElseThrow(() -> new NotFoundException("Група", id));
    }

    @Transactional
    public GroupResponse create(GroupRequest request) {
        String name = request.name().trim();
        if (groupRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Група «" + name + "» уже існує");
        }
        StudyGroup group = new StudyGroup(name, specialtyService.getEntity(request.specialtyId()),
                request.course(), request.admissionYear());
        return CatalogMapper.toResponse(groupRepository.save(group));
    }

    @Transactional
    public GroupResponse update(Long id, GroupRequest request) {
        StudyGroup group = getEntity(id);
        String name = request.name().trim();
        if (groupRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("Група «" + name + "» уже існує");
        }
        group.setName(name);
        group.setSpecialty(specialtyService.getEntity(request.specialtyId()));
        group.setCourse(request.course());
        group.setAdmissionYear(request.admissionYear());
        return CatalogMapper.toResponse(group);
    }

    @Transactional
    public void delete(Long id) {
        groupRepository.delete(getEntity(id));
        groupRepository.flush();
    }
}
