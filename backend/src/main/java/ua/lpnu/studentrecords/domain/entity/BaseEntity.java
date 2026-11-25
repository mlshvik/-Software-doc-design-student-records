package ua.lpnu.studentrecords.domain.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.util.Objects;

/**
 * Базовий клас для сутностей із сурогатним ключем.
 * Рівність сутностей визначається за ідентифікатором.
 */
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Чи представляють два об'єкти ту саму сутність: той самий екземпляр або однаковий
     * ненульовий ідентифікатор. На відміну від {@link #equals(Object)}, не порівнює класи,
     * тому коректно працює з лінивими проксі Hibernate.
     */
    public boolean isSameAs(BaseEntity other) {
        if (other == null) return false;
        if (this == other) return true;
        Long thisId = getId();
        return thisId != null && thisId.equals(other.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
