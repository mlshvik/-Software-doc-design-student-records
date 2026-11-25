package ua.lpnu.studentrecords.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.lpnu.studentrecords.pattern.report.Report;
import ua.lpnu.studentrecords.pattern.report.ReportRequest;
import ua.lpnu.studentrecords.pattern.report.ReportType;
import ua.lpnu.studentrecords.service.ReportService;
import ua.lpnu.studentrecords.web.dto.ReportTypeResponse;

import java.util.List;

/**
 * Звіти. Генератор обирає фабрика (Factory Method), звіт будується за шаблонним методом
 * (Template Method) і збирається будівельником (Builder).
 */
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Звіти")
@PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    @Operation(summary = "Доступні типи звітів")
    public List<ReportTypeResponse> types() {
        return reportService.availableTypes();
    }

    @GetMapping("/{type}")
    @Operation(summary = "Сформувати звіт",
            description = "type: group-performance, teacher-statistics або academic-debts; усі фільтри необов'язкові")
    public Report generate(@Parameter(example = "group-performance") @PathVariable String type,
                           @RequestParam(required = false) String academicYear,
                           @RequestParam(required = false) Integer semester,
                           @RequestParam(required = false) Long facultyId,
                           @RequestParam(required = false) Integer course) {
        return reportService.generate(ReportType.fromValue(type),
                new ReportRequest(academicYear, semester, facultyId, course));
    }
}
