package ua.lpnu.studentrecords.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.lpnu.studentrecords.domain.enums.DebtStatus;
import ua.lpnu.studentrecords.domain.enums.Role;
import ua.lpnu.studentrecords.security.CurrentUserService;
import ua.lpnu.studentrecords.service.DebtService;
import ua.lpnu.studentrecords.web.dto.DebtResponse;

import java.util.List;

/**
 * Академічні заборгованості. Викладач бачить лише борги зі своїх дисциплін;
 * студент – у власній заліковій книжці ({@code /api/students/{id}/record}).
 */
@RestController
@RequestMapping("/api/debts")
@Tag(name = "Академічні заборгованості")
public class DebtController {

    private final DebtService debtService;
    private final CurrentUserService currentUser;

    public DebtController(DebtService debtService, CurrentUserService currentUser) {
        this.debtService = debtService;
        this.currentUser = currentUser;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @Operation(summary = "Пошук заборгованостей за статусом, групою і студентом")
    public List<DebtResponse> search(@RequestParam(required = false) DebtStatus status,
                                     @RequestParam(required = false) Long groupId,
                                     @RequestParam(required = false) Long studentId) {
        Long teacherId = currentUser.hasRole(Role.TEACHER) ? currentUser.requireTeacherId() : null;
        return debtService.search(status, groupId, studentId, teacherId);
    }
}
