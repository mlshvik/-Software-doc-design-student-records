package ua.lpnu.studentrecords.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Налаштування системи у форматі «ключ – значення»
 * (наприклад, {@code gpa.strategy = CREDIT_WEIGHTED}).
 */
@Entity
@Table(name = "system_setting")
public class SystemSetting {

    /** Ключ налаштування, що визначає спосіб розрахунку середнього балу. */
    public static final String GPA_STRATEGY = "gpa.strategy";

    @Id
    @Column(name = "setting_key", length = 100)
    private String key;

    @Column(name = "setting_value", nullable = false, length = 255)
    private String value;

    @Column(length = 255)
    private String description;

    protected SystemSetting() {
    }

    public SystemSetting(String key, String value, String description) {
        this.key = key;
        this.value = value;
        this.description = description;
    }

    public String getKey() { return key; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
