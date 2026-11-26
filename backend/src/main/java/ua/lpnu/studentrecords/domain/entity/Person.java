package ua.lpnu.studentrecords.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

/**
 * Спільні дані особи (ПІБ, контакти) для студента і викладача.
 */
@MappedSuperclass
public abstract class Person extends BaseEntity {

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(length = 150)
    private String email;

    /** Повне ім'я у форматі «Прізвище Ім'я По батькові». */
    public String getFullName() {
        return middleName == null || middleName.isBlank()
                ? lastName + " " + firstName
                : lastName + " " + firstName + " " + middleName;
    }

    /** Скорочене ім'я у форматі «Прізвище І. Б.». */
    public String getShortName() {
        StringBuilder sb = new StringBuilder(lastName).append(' ').append(firstName.charAt(0)).append('.');
        if (middleName != null && !middleName.isBlank()) {
            sb.append(' ').append(middleName.charAt(0)).append('.');
        }
        return sb.toString();
    }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
