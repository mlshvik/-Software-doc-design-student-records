package ua.lpnu.studentrecords.pattern.report;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Незмінний (immutable) звіт: заголовок, час створення, колонки, рядки і підсумки.
 * <p>
 * <b>Шаблон Builder.</b> Звіт має багато частин, які генератор заповнює поступово
 * (колонки → рядки → підсумки). Конструктор з десятком параметрів був би незручним і
 * дозволяв би створити неповний звіт, тому об'єкт збирається через {@link Builder}:
 * <pre>{@code
 * Report report = Report.builder(ReportType.ACADEMIC_DEBTS)
 *         .title("Академічні заборгованості")
 *         .column(ReportColumn.text("student", "Студент"))
 *         .row(Map.of("student", "Маліш В. О."))
 *         .summary("total", 1)
 *         .build();
 * }</pre>
 * Метод {@link Builder#build()} перевіряє цілісність (є заголовок і колонки, рядки містять лише
 * відомі колонки) і робить захисні копії колекцій, тож готовий звіт змінити неможливо.
 */
public final class Report {

    private final ReportType type;
    private final String title;
    private final LocalDateTime generatedAt;
    private final Map<String, Object> parameters;
    private final List<ReportColumn> columns;
    private final List<Map<String, Object>> rows;
    private final Map<String, Object> summary;

    private Report(Builder builder) {
        this.type = builder.type;
        this.title = builder.title;
        this.generatedAt = builder.generatedAt != null ? builder.generatedAt : LocalDateTime.now();
        this.parameters = immutableMap(builder.parameters);
        this.columns = List.copyOf(builder.columns);
        List<Map<String, Object>> copiedRows = new ArrayList<>(builder.rows.size());
        for (Map<String, Object> row : builder.rows) {
            copiedRows.add(immutableMap(row));
        }
        this.rows = Collections.unmodifiableList(copiedRows);
        this.summary = immutableMap(builder.summary);
    }

    public static Builder builder(ReportType type) {
        return new Builder(type);
    }

    public ReportType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public List<ReportColumn> getColumns() {
        return columns;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public Map<String, Object> getSummary() {
        return summary;
    }

    public int getRowCount() {
        return rows.size();
    }

    /** Копія зі збереженням порядку ключів; значення {@code null} дозволені (на відміну від {@code Map.copyOf}). */
    private static Map<String, Object> immutableMap(Map<String, Object> source) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }

    /**
     * Покроковий будівельник звіту. Не потокобезпечний, використовується в межах одного виклику генератора.
     */
    public static final class Builder {

        private final ReportType type;
        private String title;
        private LocalDateTime generatedAt;
        private final Map<String, Object> parameters = new LinkedHashMap<>();
        private final List<ReportColumn> columns = new ArrayList<>();
        private final List<Map<String, Object>> rows = new ArrayList<>();
        private final Map<String, Object> summary = new LinkedHashMap<>();

        private Builder(ReportType type) {
            this.type = Objects.requireNonNull(type, "type");
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder generatedAt(LocalDateTime generatedAt) {
            this.generatedAt = generatedAt;
            return this;
        }

        /** Параметр звіту; {@code null}-значення пропускаються, щоб у звіті були лише застосовані фільтри. */
        public Builder parameter(String name, Object value) {
            if (value != null) {
                parameters.put(name, value);
            }
            return this;
        }

        public Builder column(ReportColumn column) {
            columns.add(Objects.requireNonNull(column, "column"));
            return this;
        }

        public Builder columns(List<ReportColumn> columns) {
            columns.forEach(this::column);
            return this;
        }

        public Builder row(Map<String, Object> row) {
            rows.add(new LinkedHashMap<>(Objects.requireNonNull(row, "row")));
            return this;
        }

        public Builder rows(List<Map<String, Object>> rows) {
            rows.forEach(this::row);
            return this;
        }

        public Builder summary(String key, Object value) {
            summary.put(key, value);
            return this;
        }

        public Builder summary(Map<String, Object> values) {
            summary.putAll(values);
            return this;
        }

        /**
         * Створює незмінний звіт.
         *
         * @throws IllegalStateException якщо немає заголовка або колонок, ключі колонок повторюються
         *                               чи рядок містить невідому колонку
         */
        public Report build() {
            if (title == null || title.isBlank()) {
                throw new IllegalStateException("Звіт повинен мати заголовок");
            }
            if (columns.isEmpty()) {
                throw new IllegalStateException("Звіт повинен мати хоча б одну колонку");
            }
            Set<String> keys = columns.stream().map(ReportColumn::key).collect(Collectors.toSet());
            if (keys.size() != columns.size()) {
                throw new IllegalStateException("Ключі колонок звіту повторюються");
            }
            for (Map<String, Object> row : rows) {
                for (String key : row.keySet()) {
                    if (!keys.contains(key)) {
                        throw new IllegalStateException("Рядок звіту містить невідому колонку: " + key);
                    }
                }
            }
            return new Report(this);
        }
    }
}
