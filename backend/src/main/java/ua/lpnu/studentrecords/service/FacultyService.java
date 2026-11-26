package ua.lpnu.studentrecords.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lpnu.studentrecords.domain.entity.Faculty;
import ua.lpnu.studentrecords.domain.exception.ConflictException;
import ua.lpnu.studentrecords.domain.exception.NotFoundException;
import ua.lpnu.studentrecords.repository.FacultyRepository;
import ua.lpnu.studentrecords.web.dto.FacultyRequest;
import ua.lpnu.studentrecords.web.dto.FacultyResponse;
import ua.lpnu.studentrecords.web.mapper.CatalogMapper;

import java.util.List;

/**
 * Довідник факультетів (інститутів).
 */
@Service
@Transactional(readOnly = true)
public class FacultyService {

    private final FacultyRepository facultyRepository;

    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public List<FacultyResponse> findAll() {
        return facultyRepository.findAllByOrderByNameAsc().stream().map(CatalogMapper::toResponse).toList();
    }

    public FacultyResponse get(Long id) {
        return CatalogMapper.toResponse(getEntity(id));
    }

    Faculty getEntity(Long id) {
        return facultyRepository.findById(id).orElseThrow(() -> new NotFoundException("Факультет", id));
    }

    @Transactional
    public FacultyResponse create(FacultyRequest request) {
        if (facultyRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ConflictException("Факультет з назвою «" + request.name().trim() + "» уже існує");
        }
        Faculty faculty = new Faculty(request.name().trim(), request.shortName().trim());
        return CatalogMapper.toResponse(facultyRepository.save(faculty));
    }

    @Transactional
    public FacultyResponse update(Long id, FacultyRequest request) {
        Faculty faculty = getEntity(id);
        if (facultyRepository.existsByNameIgnoreCaseAndIdNot(request.name().trim(), id)) {
            throw new ConflictException("Факультет з назвою «" + request.name().trim() + "» уже існує");
        }
        faculty.setName(request.name().trim());
        faculty.setShortName(request.shortName().trim());
        return CatalogMapper.toResponse(faculty);
    }

    /** Видалення; якщо на факультет посилаються спеціальності, БД відхилить операцію (HTTP 409). */
    @Transactional
    public void delete(Long id) {
        facultyRepository.delete(getEntity(id));
        facultyRepository.flush();
    }
}
