package ua.lpnu.studentrecords.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.lpnu.studentrecords.service.SettingService;
import ua.lpnu.studentrecords.web.dto.SettingResponse;
import ua.lpnu.studentrecords.web.dto.SettingUpdateRequest;

import java.util.List;

@RestController
@RequestMapping("/api/settings")
@Tag(name = "Налаштування")
@PreAuthorize("hasRole('ADMIN')")
public class SettingController {

    private final SettingService settingService;

    public SettingController(SettingService settingService) {
        this.settingService = settingService;
    }

    @GetMapping
    @Operation(summary = "Системні налаштування")
    public List<SettingResponse> findAll() {
        return settingService.findAll();
    }

    @PutMapping("/{key}")
    @Operation(summary = "Змінити налаштування",
            description = "Напр., gpa.strategy = SIMPLE або CREDIT_WEIGHTED (вибір стратегії середнього балу)")
    public SettingResponse update(@PathVariable String key, @Valid @RequestBody SettingUpdateRequest request) {
        return settingService.update(key, request.value());
    }
}
