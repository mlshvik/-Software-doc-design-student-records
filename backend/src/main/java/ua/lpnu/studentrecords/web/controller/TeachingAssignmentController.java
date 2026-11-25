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
import ua.lpnu.studentrecords.domain.enums.Role;
import ua.lpnu.studentrecords.security.CurrentUserService;
import ua.lpnu.studentrecords.service.GradeService;
import ua.lpnu.studentrecords.service.TeachingAssignmentService;
import ua.lpnu.studentrecords.web.dto.AssignmentRequest;
import ua.lpnu.studentrecords.web.dto.AssignmentResponse;
import ua.lpnu.studentrecords.web.dto.GradeSheetResponse;

import java.util.List;

/**
 * Призначення викладачів на дисципліни. Викладач бачить лише власні призначення.
 */
@RestController
@RequestMapping("/api/assignments")
@Tag(name = "Призначення (навантаження)")
public class TeachingAssignmentController {

    private final TeachingAssignmentService assignmentService;
    private final GradeService gradeService;
    private final CurrentUserService currentUser;

    public TeachingAssignmentController(TeachingAssignmentService assignmentService,
                                        GradeService gradeService,
                                        CurrentUserService currentUser) {
        this.assignmentService = assignmentService;
        this.gradeService = gradeService;
        this.currentUser = currentUser;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Список призначень",
            description = "Для викладача фільтр teacherId завжди дорівнює його власному ідентифікатору")
    public List<AssignmentResponse> find(@RequestParam(required = false) Long teacherId,
                                         @RequestParam(required = false) Long groupId,
                                         @RequestParam(required = false) String academicYear,
                                         @RequestParam(required = false) Integer semester) {
        Long effectiveTeacherId = currentUser.hasRole(Role.TEACHER) ? currentUser.requireTeacherId() : teacherId;
        return assignmentService.find(effectiveTeacherId, groupId, academicYear, semester);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@access.canManageAssignment(#id)")
    @Operation(summary = "Призначення за ідентифікатором")
    public AssignmentResponse get(@PathVariable Long id) {
        return assignmentService.get(id);
    }

    @GetMapping("/{id}/sheet")
    @PreAuthorize("@access.canManageAssignment(#id)")
    @Operation(summary = "Відомість групи з дисципліни",
            description = "Студенти групи, їхні спроби, підсумкова оцінка і наступна допустима спроба")
    public GradeSheetResponse sheet(@PathVariable Long id) {
        return gradeService.getGradeSheet(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Створити призначення")
    public AssignmentResponse create(@Valid @RequestBody AssignmentRequest request) {
        return assignmentService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Змінити призначення")
    public AssignmentResponse update(@PathVariable Long id, @Valid @RequestBody AssignmentRequest request) {
        return assignmentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Видалити призначення", description = "Неможливо, якщо за ним уже є оцінки або заняття (409)")
    public void delete(@PathVariable Long id) {
        assignmentService.delete(id);
    }
}
