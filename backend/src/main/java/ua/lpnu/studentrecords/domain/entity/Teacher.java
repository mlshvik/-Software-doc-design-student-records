package ua.lpnu.studentrecords.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Викладач.
 */
@Entity
@Table(name = "teacher")
public class Teacher extends Person {

    /** Посада (асистент, старший викладач, доцент, професор). */
    @Column(length = 100)
    private String position;

    /** Науковий ступінь (к.т.н., д.т.н.). */
    @Column(name = "academic_degree", length = 100)
    private String academicDegree;

    /** Кафедра. */
    @Column(length = 200)
    private String department;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private AppUser user;

    protected Teacher() {
    }

    public Teacher(String lastName, String firstName, String middleName, String position, String department) {
        setLastName(lastName);
        setFirstName(firstName);
        setMiddleName(middleName);
        this.position = position;
        this.department = department;
    }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getAcademicDegree() { return academicDegree; }
    public void setAcademicDegree(String academicDegree) { this.academicDegree = academicDegree; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
}
