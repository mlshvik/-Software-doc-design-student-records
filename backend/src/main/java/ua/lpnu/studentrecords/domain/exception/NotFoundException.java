package ua.lpnu.studentrecords.domain.exception;

/**
 * Запитаний об'єкт не знайдено (відповідає HTTP 404).
 */
public class NotFoundException extends BusinessException {

    private final String entityName;
    private final Object id;

    public NotFoundException(String entityName, Object id) {
        super(entityName + " з ідентифікатором " + id + " не знайдено");
        this.entityName = entityName;
        this.id = id;
    }

    public String getEntityName() {
        return entityName;
    }

    public Object getId() {
        return id;
    }
}
