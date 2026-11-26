package ua.lpnu.studentrecords.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ua.lpnu.studentrecords.repository.StudentSearchCriteria;
import ua.lpnu.studentrecords.service.StudentService;
import ua.lpnu.studentrecords.service.StudyGroupService;
import ua.lpnu.studentrecords.web.dto.GroupRequest;
import ua.lpnu.studentrecords.web.dto.GroupResponse;
import ua.lpnu.studentrecords.web.dto.StudentResponse;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@Tag(name = "Академічні групи")
public class StudyGroupController {

    private final StudyGroupService groupService;
    private final StudentService studentService;

    public StudyGroupController(StudyGroupService groupService, StudentService studentService) {
        this.groupService = groupService;
        this.studentService = studentService;
    }

    @GetMapping
    @Operation(summary = "Список груп з фільтрами за факультетом, спеціальністю і курсом")
    public List<GroupResponse> findAll(@RequestParam(required = false) Long facultyId,
                                       @RequestParam(required = false) Long specialtyId,
                                       @RequestParam(required = false) Integer course) {
        return groupService.findAll(facultyId, specialtyId, course);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Група за ідентифікатором")
    public GroupResponse get(@PathVariable Long id) {
        return groupService.get(id);
    }

    @GetMapping("/{id}/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Студенти групи")
    public List<StudentResponse> students(@PathVariable Long id) {
        return studentService.searchStudents(new StudentSearchCriteria(null, null, id, null, null, null));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Створити групу")
    public GroupResponse create(@Valid @RequestBody GroupRequest request) {
        return groupService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Змінити групу")
    public GroupResponse update(@PathVariable Long id, @Valid @RequestBody GroupRequest request) {
        return groupService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Видалити групу", description = "Неможливо, якщо в групі є студенти (409)")
    public void delete(@PathVariable Long id) {
        groupService.delete(id);
    }
}
