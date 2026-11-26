package ua.lpnu.studentrecords.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import ua.lpnu.studentrecords.domain.enums.ControlType;

import java.math.BigDecimal;

/**
 * Навчальна дисципліна (предмет).
 */
@Entity
@Table(name = "subject")
public class Subject extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    /** Кількість кредитів ЄКТС. */
    @Column(nullable = false, precision = 4, scale = 1)
    private BigDecimal credits;

    /** Загальна кількість годин. */
    @Column(nullable = false)
    private Integer hours;

    @Enumerated(EnumType.STRING)
    @Column(name = "control_type", nullable = false, length = 20)
    private ControlType controlType;

    protected Subject() {
    }

    public Subject(String name, BigDecimal credits, Integer hours, ControlType controlType) {
        this.name = name;
        this.credits = credits;
        this.hours = hours;
        this.controlType = controlType;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getCredits() { return credits; }
    public void setCredits(BigDecimal credits) { this.credits = credits; }

    public Integer getHours() { return hours; }
    public void setHours(Integer hours) { this.hours = hours; }

    public ControlType getControlType() { return controlType; }
    public void setControlType(ControlType controlType) { this.controlType = controlType; }
}
