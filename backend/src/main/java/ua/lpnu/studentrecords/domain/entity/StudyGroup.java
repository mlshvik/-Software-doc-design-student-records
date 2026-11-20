package ua.lpnu.studentrecords.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Академічна група (наприклад, ІР-43).
 */
@Entity
@Table(name = "study_group")
public class StudyGroup extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    /** Курс навчання (1–6). */
    @Column(nullable = false)
    private Integer course;

    @Column(name = "admission_year", nullable = false)
    private Integer admissionYear;

    protected StudyGroup() {
    }

    public StudyGroup(String name, Specialty specialty, Integer course, Integer admissionYear) {
        this.name = name;
        this.specialty = specialty;
        this.course = course;
        this.admissionYear = admissionYear;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Specialty getSpecialty() { return specialty; }
    public void setSpecialty(Specialty specialty) { this.specialty = specialty; }

    public Integer getCourse() { return course; }
    public void setCourse(Integer course) { this.course = course; }

    public Integer getAdmissionYear() { return admissionYear; }
    public void setAdmissionYear(Integer admissionYear) { this.admissionYear = admissionYear; }
}
