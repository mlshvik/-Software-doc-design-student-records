package ua.lpnu.studentrecords.domain.exception;

/**
 * Порушення унікальності або цілісності даних: запис з таким значенням уже існує
 * чи на нього посилаються інші дані (HTTP 409).
 */
public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super(message);
    }
}
