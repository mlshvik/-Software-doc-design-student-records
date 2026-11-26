package ua.lpnu.studentrecords.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Факультет (інститут) закладу вищої освіти.
 */
@Entity
@Table(name = "faculty")
public class Faculty extends BaseEntity {

    @Column(nullable = false, unique = true, length = 200)
    private String name;

    @Column(name = "short_name", nullable = false, length = 20)
    private String shortName;

    protected Faculty() {
    }

    public Faculty(String name, String shortName) {
        this.name = name;
        this.shortName = shortName;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }
}
