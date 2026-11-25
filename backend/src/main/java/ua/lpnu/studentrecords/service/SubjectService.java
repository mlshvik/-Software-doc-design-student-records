package ua.lpnu.studentrecords.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lpnu.studentrecords.domain.entity.Subject;
import ua.lpnu.studentrecords.domain.exception.NotFoundException;
import ua.lpnu.studentrecords.repository.SubjectRepository;
import ua.lpnu.studentrecords.web.dto.SubjectRequest;
import ua.lpnu.studentrecords.web.dto.SubjectResponse;
import ua.lpnu.studentrecords.web.mapper.CatalogMapper;

import java.util.List;

/**
 * Довідник навчальних дисциплін.
 */
@Service
@Transactional(readOnly = true)
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public List<SubjectResponse> findAll() {
        return subjectRepository.findAllByOrderByNameAsc().stream().map(CatalogMapper::toResponse).toList();
    }

    public SubjectResponse get(Long id) {
        return CatalogMapper.toResponse(getEntity(id));
    }

    Subject getEntity(Long id) {
        return subjectRepository.findById(id).orElseThrow(() -> new NotFoundException("Дисципліна", id));
    }

    @Transactional
    public SubjectResponse create(SubjectRequest request) {
        Subject subject = new Subject(request.name().trim(), request.credits(), request.hours(), request.controlType());
        return CatalogMapper.toResponse(subjectRepository.save(subject));
    }

    @Transactional
    public SubjectResponse update(Long id, SubjectRequest request) {
        Subject subject = getEntity(id);
        subject.setName(request.name().trim());
        subject.setCredits(request.credits());
        subject.setHours(request.hours());
        subject.setControlType(request.controlType());
        return CatalogMapper.toResponse(subject);
    }

    @Transactional
    public void delete(Long id) {
        subjectRepository.delete(getEntity(id));
        subjectRepository.flush();
    }
}
