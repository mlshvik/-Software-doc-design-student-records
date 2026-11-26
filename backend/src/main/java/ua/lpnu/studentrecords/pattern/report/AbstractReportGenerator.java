package ua.lpnu.studentrecords.pattern.report;

import java.util.List;
import java.util.Map;

/**
 * Базовий клас генераторів звітів.
 * <p>
 * <b>Шаблон Template Method.</b> Усі звіти будуються за однаковим алгоритмом:
 * <ol>
 *     <li>{@link #loadData(ReportRequest)} – завантажити дані з репозиторіїв з урахуванням фільтрів;</li>
 *     <li>{@link #aggregate(Object, ReportRequest)} – згрупувати і порахувати показники (рядки звіту);</li>
 *     <li>{@link #summarize(Object, List)} – підсумки (необов'язковий крок-«гачок», hook);</li>
 *     <li>зібрати незмінний {@link Report} через {@link Report.Builder}.</li>
 * </ol>
 * Порядок кроків зафіксовано у {@code final}-методі {@link #generate(ReportRequest)}, тож підкласи
 * не можуть його порушити, а лише реалізують окремі кроки. Новий звіт = новий підклас із трьома
 * методами, загальний код (фільтри, параметри, валідація, збирання) не дублюється.
 * <p>
 * Генератори не мають транзакційних анотацій (CGLIB-проксі не перевизначає {@code final}-метод);
 * транзакцію відкриває {@code ReportService}, який отримує генератор від
 * {@link ReportGeneratorFactory}.
 *
 * @param <D> тип «сирих» даних, з яких будується звіт
 */
public abstract class AbstractReportGenerator<D> {

    /**
     * Шаблонний метод: незмінна послідовність кроків побудови звіту.
     *
     * @param request фільтри звіту; {@code null} означає «без фільтрів»
     */
    public final Report generate(ReportRequest request) {
        ReportRequest effective = request != null ? request : ReportRequest.empty();
        D data = loadData(effective);
        List<Map<String, Object>> rows = aggregate(data, effective);
        Map<String, Object> summary = summarize(data, rows);
        return Report.builder(type())
                .title(title(effective))
                .parameter("academicYear", effective.academicYear())
                .parameter("semester", effective.semester())
                .parameter("facultyId", effective.facultyId())
                .parameter("course", effective.course())
                .columns(columns())
                .rows(rows)
                .summary(summary)
                .build();
    }

    /** Тип звіту, за яким генератор реєструється у фабриці. */
    public abstract ReportType type();

    /** Крок 1: завантаження даних. */
    protected abstract D loadData(ReportRequest request);

    /** Крок 2: агрегування – рядки звіту, ключі яких відповідають {@link #columns()}. */
    protected abstract List<Map<String, Object>> aggregate(D data, ReportRequest request);

    /** Колонки звіту. */
    protected abstract List<ReportColumn> columns();

    /** Гачок: підсумкові показники. За замовчуванням підсумків немає. */
    protected Map<String, Object> summarize(D data, List<Map<String, Object>> rows) {
        return Map.of();
    }

    /** Гачок: заголовок звіту. За замовчуванням – назва типу. */
    protected String title(ReportRequest request) {
        return request.academicYear() == null
                ? type().title()
                : type().title() + " (" + request.academicYear() + ")";
    }
}
