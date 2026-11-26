package ua.lpnu.studentrecords.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lpnu.studentrecords.domain.entity.Teacher;
import ua.lpnu.studentrecords.domain.exception.NotFoundException;
import ua.lpnu.studentrecords.repository.TeacherRepository;
import ua.lpnu.studentrecords.web.dto.TeacherRequest;
import ua.lpnu.studentrecords.web.dto.TeacherResponse;
import ua.lpnu.studentrecords.web.mapper.PersonMapper;

import java.util.List;

/**
 * Облік викладачів.
 */
@Service
@Transactional(readOnly = true)
public class TeacherService {

    private final TeacherRepository teacherRepository;

    public TeacherService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    public List<TeacherResponse> findAll() {
        return teacherRepository.findAllByOrderByLastNameAscFirstNameAsc().stream()
                .map(PersonMapper::toResponse)
                .toList();
    }

    public TeacherResponse get(Long id) {
        return PersonMapper.toResponse(getEntity(id));
    }

    Teacher getEntity(Long id) {
        return teacherRepository.findById(id).orElseThrow(() -> new NotFoundException("Викладач", id));
    }

    @Transactional
    public TeacherResponse create(TeacherRequest request) {
        Teacher teacher = new Teacher(request.lastName().trim(), request.firstName().trim(), null, null, null);
        PersonMapper.apply(request, teacher);
        return PersonMapper.toResponse(teacherRepository.save(teacher));
    }

    @Transactional
    public TeacherResponse update(Long id, TeacherRequest request) {
        Teacher teacher = getEntity(id);
        PersonMapper.apply(request, teacher);
        return PersonMapper.toResponse(teacher);
    }

    @Transactional
    public void delete(Long id) {
        teacherRepository.delete(getEntity(id));
        teacherRepository.flush();
    }
}
