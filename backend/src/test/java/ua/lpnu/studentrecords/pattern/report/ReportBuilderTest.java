package ua.lpnu.studentrecords.pattern.report;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тести шаблону Builder: покрокове збирання звіту, перевірка цілісності і незмінність результату.
 */
class ReportBuilderTest {

    private static Report.Builder validBuilder() {
        return Report.builder(ReportType.ACADEMIC_DEBTS)
                .title("Академічні заборгованості")
                .column(ReportColumn.text("student", "Студент"))
                .column(ReportColumn.number("debts", "Боргів"));
    }

    @Test
    void buildsReportWithAllParts() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("student", "Маліш В. О.");
        row.put("debts", 2);

        Report report = validBuilder()
                .parameter("academicYear", "2025/2026")
                .parameter("course", null)
                .row(row)
                .summary("total", 2)
                .build();

        assertEquals(ReportType.ACADEMIC_DEBTS, report.getType());
        assertEquals("Академічні заборгованості", report.getTitle());
        assertNotNull(report.getGeneratedAt());
        assertEquals(List.of("student", "debts"), report.getColumns().stream().map(ReportColumn::key).toList());
        assertEquals(1, report.getRowCount());
        assertEquals("Маліш В. О.", report.getRows().get(0).get("student"));
        assertEquals(2, report.getSummary().get("total"));
        assertEquals(Map.of("academicYear", "2025/2026"), report.getParameters(), "null-параметри пропускаються");
    }

    @Test
    void reportIsImmutable() {
        Map<String, Object> row = new HashMap<>();
        row.put("student", "Маліш В. О.");
        Report.Builder builder = validBuilder().row(row);
        Report report = builder.build();

        // зміни вихідних даних і будівельника після build() не впливають на звіт
        row.put("student", "Інше ім'я");
        builder.row(Map.of("student", "Ще один рядок"));
        assertEquals(1, report.getRowCount());
        assertEquals("Маліш В. О.", report.getRows().get(0).get("student"));

        assertThrows(UnsupportedOperationException.class, () -> report.getRows().add(Map.of()));
        assertThrows(UnsupportedOperationException.class, () -> report.getRows().get(0).put("debts", 1));
        assertThrows(UnsupportedOperationException.class,
                () -> report.getColumns().add(ReportColumn.text("x", "X")));
        assertThrows(UnsupportedOperationException.class, () -> report.getSummary().put("x", 1));
    }

    @Test
    void allowsNullValuesInRows() {
        Map<String, Object> row = new HashMap<>();
        row.put("student", "Маліш В. О.");
        row.put("debts", null);

        Report report = validBuilder().row(row).build();

        assertTrue(report.getRows().get(0).containsKey("debts"));
        assertNull(report.getRows().get(0).get("debts"));
    }

    @Test
    void requiresTitleAndColumns() {
        assertThrows(IllegalStateException.class, () -> Report.builder(ReportType.ACADEMIC_DEBTS)
                .column(ReportColumn.text("student", "Студент"))
                .build());
        assertThrows(IllegalStateException.class, () -> Report.builder(ReportType.ACADEMIC_DEBTS)
                .title("Без колонок")
                .build());
    }

    @Test
    void rejectsUnknownAndDuplicateColumns() {
        assertThrows(IllegalStateException.class, () -> validBuilder().row(Map.of("unknown", 1)).build());
        assertThrows(IllegalStateException.class,
                () -> validBuilder().column(ReportColumn.text("student", "Ще раз студент")).build());
    }

    @Test
    void columnFactoriesMarkNumericColumns() {
        assertTrue(ReportColumn.number("avg", "Середній бал").numeric());
        assertFalse(ReportColumn.text("group", "Група").numeric());
    }
}
