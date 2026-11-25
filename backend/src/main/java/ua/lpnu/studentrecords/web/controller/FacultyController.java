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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ua.lpnu.studentrecords.service.FacultyService;
import ua.lpnu.studentrecords.web.dto.FacultyRequest;
import ua.lpnu.studentrecords.web.dto.FacultyResponse;

import java.util.List;

@RestController
@RequestMapping("/api/faculties")
@Tag(name = "Факультети")
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping
    @Operation(summary = "Список факультетів")
    public List<FacultyResponse> findAll() {
        return facultyService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Факультет за ідентифікатором")
    public FacultyResponse get(@PathVariable Long id) {
        return facultyService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Створити факультет")
    public FacultyResponse create(@Valid @RequestBody FacultyRequest request) {
        return facultyService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Змінити факультет")
    public FacultyResponse update(@PathVariable Long id, @Valid @RequestBody FacultyRequest request) {
        return facultyService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Видалити факультет", description = "Неможливо, якщо до факультету належать спеціальності (409)")
    public void delete(@PathVariable Long id) {
        facultyService.delete(id);
    }
}
