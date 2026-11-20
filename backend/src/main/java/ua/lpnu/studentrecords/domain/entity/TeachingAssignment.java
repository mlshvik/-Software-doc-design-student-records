package ua.lpnu.studentrecords.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Призначення викладача: дисципліна, яку певний викладач веде в певній групі
 * у певному семестрі навчального року. Саме до призначення прив'язуються
 * оцінки, заборгованості та заняття в розкладі.
 */
@Entity
@Table(name = "teaching_assignment",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_assignment_subject_group_semester",
                columnNames = {"subject_id", "group_id", "semester", "academic_year"}))
public class TeachingAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private StudyGroup group;

    /** Номер семестру (1–12). */
    @Column(nullable = false)
    private Integer semester;

    /** Навчальний рік у форматі «2026/2027». */
    @Column(name = "academic_year", nullable = false, length = 9)
    private String academicYear;

    protected TeachingAssignment() {
    }

    public TeachingAssignment(Subject subject, Teacher teacher, StudyGroup group,
                              Integer semester, String academicYear) {
        this.subject = subject;
        this.teacher = teacher;
        this.group = group;
        this.semester = semester;
        this.academicYear = academicYear;
    }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }

    public StudyGroup getGroup() { return group; }
    public void setGroup(StudyGroup group) { this.group = group; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
}
