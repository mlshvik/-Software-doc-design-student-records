package ua.lpnu.studentrecords.pattern.report;

/**
 * Колонка табличного звіту.
 *
 * @param key     ключ значення в рядку звіту
 * @param title   заголовок колонки українською
 * @param numeric чи є значення числовим (для вирівнювання і побудови графіків на клієнті)
 */
public record ReportColumn(String key, String title, boolean numeric) {

    public static ReportColumn text(String key, String title) {
        return new ReportColumn(key, title, false);
    }

    public static ReportColumn number(String key, String title) {
        return new ReportColumn(key, title, true);
    }
}
