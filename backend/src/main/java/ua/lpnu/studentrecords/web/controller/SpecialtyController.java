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
import ua.lpnu.studentrecords.service.SpecialtyService;
import ua.lpnu.studentrecords.web.dto.SpecialtyRequest;
import ua.lpnu.studentrecords.web.dto.SpecialtyResponse;

import java.util.List;

@RestController
@RequestMapping("/api/specialties")
@Tag(name = "Спеціальності")
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    public SpecialtyController(SpecialtyService specialtyService) {
        this.specialtyService = specialtyService;
    }

    @GetMapping
    @Operation(summary = "Список спеціальностей (усі або факультету)")
    public List<SpecialtyResponse> findAll(@RequestParam(required = false) Long facultyId) {
        return specialtyService.findAll(facultyId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Спеціальність за ідентифікатором")
    public SpecialtyResponse get(@PathVariable Long id) {
        return specialtyService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Створити спеціальність")
    public SpecialtyResponse create(@Valid @RequestBody SpecialtyRequest request) {
        return specialtyService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Змінити спеціальність")
    public SpecialtyResponse update(@PathVariable Long id, @Valid @RequestBody SpecialtyRequest request) {
        return specialtyService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Видалити спеціальність")
    public void delete(@PathVariable Long id) {
        specialtyService.delete(id);
    }
}
