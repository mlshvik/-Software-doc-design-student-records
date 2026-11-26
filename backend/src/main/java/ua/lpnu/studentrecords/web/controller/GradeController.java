package ua.lpnu.studentrecords.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ua.lpnu.studentrecords.domain.enums.Role;
import ua.lpnu.studentrecords.security.CurrentUserService;
import ua.lpnu.studentrecords.service.GradeService;
import ua.lpnu.studentrecords.web.dto.GradeRequest;
import ua.lpnu.studentrecords.web.dto.GradeResponse;

/**
 * Виставлення оцінок.
 * <p>
 * Контролер перевіряє лише роль. Бізнес-правила (діапазон балу, статус студента, чи веде викладач
 * дисципліну, черговість перескладань) перевіряє ланцюжок Chain of Responsibility у
 * {@link GradeService}; порушення повертається як 422 з назвою правила.
 */
@RestController
@RequestMapping("/api/grades")
@Tag(name = "Оцінки")
public class GradeController {

    private final GradeService gradeService;
    private final CurrentUserService currentUser;

    public GradeController(GradeService gradeService, CurrentUserService currentUser) {
        this.gradeService = gradeService;
        this.currentUser = currentUser;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Виставити оцінку або перескладання",
            description = "Якщо attempt не вказано, система визначить наступну спробу автоматично. "
                    + "Незадовільна оцінка відкриває академічну заборгованість, позитивна – закриває (Observer).")
    public GradeResponse record(@Valid @RequestBody GradeRequest request) {
        Long teacherId = currentUser.hasRole(Role.TEACHER) ? currentUser.requireTeacherId() : null;
        return gradeService.record(request, teacherId);
    }
}
