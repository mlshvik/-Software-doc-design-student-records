package ua.lpnu.studentrecords.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lpnu.studentrecords.domain.entity.Specialty;
import ua.lpnu.studentrecords.domain.exception.NotFoundException;
import ua.lpnu.studentrecords.repository.SpecialtyRepository;
import ua.lpnu.studentrecords.web.dto.SpecialtyRequest;
import ua.lpnu.studentrecords.web.dto.SpecialtyResponse;
import ua.lpnu.studentrecords.web.mapper.CatalogMapper;

import java.util.List;

/**
 * Довідник спеціальностей.
 */
@Service
@Transactional(readOnly = true)
public class SpecialtyService {

    private final SpecialtyRepository specialtyRepository;
    private final FacultyService facultyService;

    public SpecialtyService(SpecialtyRepository specialtyRepository, FacultyService facultyService) {
        this.specialtyRepository = specialtyRepository;
        this.facultyService = facultyService;
    }

    /** Усі спеціальності або лише спеціальності факультету. */
    public List<SpecialtyResponse> findAll(Long facultyId) {
        List<Specialty> specialties = facultyId == null
                ? specialtyRepository.findAllByOrderByCodeAsc()
                : specialtyRepository.findByFacultyIdOrderByCode(facultyId);
        return specialties.stream().map(CatalogMapper::toResponse).toList();
    }

    public SpecialtyResponse get(Long id) {
        return CatalogMapper.toResponse(getEntity(id));
    }

    Specialty getEntity(Long id) {
        return specialtyRepository.findById(id).orElseThrow(() -> new NotFoundException("Спеціальність", id));
    }

    @Transactional
    public SpecialtyResponse create(SpecialtyRequest request) {
        Specialty specialty = new Specialty(request.code().trim(), request.name().trim(),
                facultyService.getEntity(request.facultyId()));
        return CatalogMapper.toResponse(specialtyRepository.save(specialty));
    }

    @Transactional
    public SpecialtyResponse update(Long id, SpecialtyRequest request) {
        Specialty specialty = getEntity(id);
        specialty.setCode(request.code().trim());
        specialty.setName(request.name().trim());
        specialty.setFaculty(facultyService.getEntity(request.facultyId()));
        return CatalogMapper.toResponse(specialty);
    }

    @Transactional
    public void delete(Long id) {
        specialtyRepository.delete(getEntity(id));
        specialtyRepository.flush();
    }
}
