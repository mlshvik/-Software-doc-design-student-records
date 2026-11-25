package ua.lpnu.studentrecords.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ua.lpnu.studentrecords.domain.enums.StudentStatus;
import ua.lpnu.studentrecords.pattern.facade.StudentRecordDto;
import ua.lpnu.studentrecords.pattern.facade.StudentRecordFacade;
import ua.lpnu.studentrecords.repository.StudentSearchCriteria;
import ua.lpnu.studentrecords.service.GpaResult;
import ua.lpnu.studentrecords.service.GpaService;
import ua.lpnu.studentrecords.service.GradeService;
import ua.lpnu.studentrecords.service.StudentService;
import ua.lpnu.studentrecords.web.dto.GradeResponse;
import ua.lpnu.studentrecords.web.dto.StatusChangeRequest;
import ua.lpnu.studentrecords.web.dto.StudentRequest;
import ua.lpnu.studentrecords.web.dto.StudentResponse;

import java.util.List;

/**
 * Студенти: пошук, картка, залікова книжка (Facade), середній бал (Strategy), зміна статусу (State).
 */
@RestController
@RequestMapping("/api/students")
@Tag(name = "Студенти")
public class StudentController {

    private final StudentService studentService;
    private final StudentRecordFacade recordFacade;
    private final GpaService gpaService;
    private final GradeService gradeService;

    public StudentController(StudentService studentService,
                             StudentRecordFacade recordFacade,
                             GpaService gpaService,
                             GradeService gradeService) {
        this.studentService = studentService;
        this.recordFacade = recordFacade;
        this.gpaService = gpaService;
        this.gradeService = gradeService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Пошук студентів",
            description = "Фільтри: факультет, спеціальність, група, курс, статус; q – фрагмент прізвища, імені або номера залікової")
    public List<StudentResponse> search(@RequestParam(required = false) Long facultyId,
                                        @RequestParam(required = false) Long specialtyId,
                                        @RequestParam(required = false) Long groupId,
                                        @RequestParam(required = false) Integer course,
                                        @RequestParam(required = false) StudentStatus status,
                                        @RequestParam(required = false) String q) {
        return studentService.searchStudents(
                new StudentSearchCriteria(facultyId, specialtyId, groupId, course, status, q));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@access.canViewStudent(#id)")
    @Operation(summary = "Картка студента")
    public StudentResponse get(@PathVariable Long id) {
        return studentService.getStudent(id);
    }

    @GetMapping("/{id}/record")
    @PreAuthorize("@access.canViewStudent(#id)")
    @Operation(summary = "Залікова книжка студента",
            description = "Оцінки, середній бал, заборгованості і розклад одним запитом (шаблон Facade)")
    public StudentRecordDto record(@PathVariable Long id) {
        return recordFacade.getRecord(id);
    }

    @GetMapping("/{id}/gpa")
    @PreAuthorize("@access.canViewStudent(#id)")
    @Operation(summary = "Середній бал студента",
            description = "Розраховується стратегією з налаштування gpa.strategy (шаблон Strategy)")
    public GpaResult gpa(@PathVariable Long id) {
        studentService.getById(id);
        return gpaService.getGpa(id);
    }

    @GetMapping("/{id}/grades")
    @PreAuthorize("@access.canViewStudent(#id)")
    @Operation(summary = "Усі оцінки студента (включно з перескладаннями)")
    public List<GradeResponse> grades(@PathVariable Long id) {
        studentService.getById(id);
        return gradeService.studentGrades(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Зарахувати студента")
    public StudentResponse create(@Valid @RequestBody StudentRequest request) {
        return studentService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Змінити дані студента")
    public StudentResponse update(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        return studentService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Змінити статус студента",
            description = "Допустимі переходи визначає поточний стан (шаблон State); недопустимий перехід – 409")
    public StudentResponse changeStatus(@PathVariable Long id, @Valid @RequestBody StatusChangeRequest request) {
        return studentService.updateStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Видалити студента", description = "Студента з оцінками видалити не можна (409) – змініть статус")
    public void delete(@PathVariable Long id) {
        studentService.delete(id);
    }
}
