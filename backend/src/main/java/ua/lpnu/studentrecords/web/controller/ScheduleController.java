package ua.lpnu.studentrecords.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
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
import ua.lpnu.studentrecords.security.AccessGuard;
import ua.lpnu.studentrecords.security.CurrentUserService;
import ua.lpnu.studentrecords.service.schedule.ScheduleService;
import ua.lpnu.studentrecords.web.dto.ScheduleConflictResponse;
import ua.lpnu.studentrecords.web.dto.ScheduleEntryRequest;
import ua.lpnu.studentrecords.web.dto.ScheduleEntryResponse;

import java.util.List;

/**
 * Розклад занять: перегляд для групи, викладача, студента і власний розклад; конструктор розкладу
 * для адміністратора з перевіркою конфліктів ({@code ScheduleConflictChecker}).
 */
@RestController
@RequestMapping("/api/schedule")
@Tag(name = "Розклад")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final CurrentUserService currentUser;
    private final AccessGuard accessGuard;

    public ScheduleController(ScheduleService scheduleService, CurrentUserService currentUser,
                              AccessGuard accessGuard) {
        this.scheduleService = scheduleService;
        this.currentUser = currentUser;
        this.accessGuard = accessGuard;
    }

    @GetMapping
    @Operation(summary = "Розклад групи, викладача або студента",
            description = "Потрібно вказати рівно один параметр: groupId, teacherId або studentId")
    public List<ScheduleEntryResponse> find(@RequestParam(required = false) Long groupId,
                                            @RequestParam(required = false) Long teacherId,
                                            @RequestParam(required = false) Long studentId) {
        int filters = (groupId != null ? 1 : 0) + (teacherId != null ? 1 : 0) + (studentId != null ? 1 : 0);
        if (filters != 1) {
            throw new IllegalArgumentException("Вкажіть рівно один параметр: groupId, teacherId або studentId");
        }
        if (groupId != null) {
            return scheduleService.groupTimetable(groupId);
        }
        if (teacherId != null) {
            return scheduleService.teacherTimetable(teacherId);
        }
        if (!accessGuard.canViewStudent(studentId)) {
            throw new AccessDeniedException("Студент може переглядати лише власний розклад");
        }
        return scheduleService.studentTimetable(studentId);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    @Operation(summary = "Мій розклад", description = "Для студента – розклад його групи, для викладача – його заняття")
    public List<ScheduleEntryResponse> mine() {
        if (currentUser.hasRole(Role.TEACHER)) {
            return scheduleService.teacherTimetable(currentUser.requireTeacherId());
        }
        return scheduleService.studentTimetable(currentUser.requireStudentId());
    }

    @PostMapping("/check")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Перевірити заняття на конфлікти без збереження",
            description = "excludeId – ідентифікатор заняття, яке редагується (щоб не конфліктувати з самим собою)")
    public List<ScheduleConflictResponse> check(@Valid @RequestBody ScheduleEntryRequest request,
                                                @RequestParam(required = false) Long excludeId) {
        return scheduleService.check(request, excludeId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Додати заняття", description = "Якщо викладач, аудиторія або група зайняті – 409 зі списком конфліктів")
    public ScheduleEntryResponse create(@Valid @RequestBody ScheduleEntryRequest request) {
        return scheduleService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Змінити заняття")
    public ScheduleEntryResponse update(@PathVariable Long id, @Valid @RequestBody ScheduleEntryRequest request) {
        return scheduleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Видалити заняття")
    public void delete(@PathVariable Long id) {
        scheduleService.delete(id);
    }
}
