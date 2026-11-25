package ua.lpnu.studentrecords.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lpnu.studentrecords.pattern.report.Report;
import ua.lpnu.studentrecords.pattern.report.ReportGeneratorFactory;
import ua.lpnu.studentrecords.pattern.report.ReportRequest;
import ua.lpnu.studentrecords.pattern.report.ReportType;
import ua.lpnu.studentrecords.web.dto.ReportTypeResponse;

import java.util.List;

/**
 * Формування звітів. Отримує потрібний генератор від фабрики (Factory Method) і запускає його
 * шаблонний метод (Template Method) в одній транзакції лише для читання.
 */
@Service
@Transactional(readOnly = true)
public class ReportService {

    private final ReportGeneratorFactory generatorFactory;

    public ReportService(ReportGeneratorFactory generatorFactory) {
        this.generatorFactory = generatorFactory;
    }

    public Report generate(ReportType type, ReportRequest request) {
        return generatorFactory.create(type).generate(request);
    }

    public List<ReportTypeResponse> availableTypes() {
        return generatorFactory.supportedTypes().stream().map(ReportTypeResponse::of).toList();
    }
}
