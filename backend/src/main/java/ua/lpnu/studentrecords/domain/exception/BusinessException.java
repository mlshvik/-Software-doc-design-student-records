package ua.lpnu.studentrecords.domain.exception;

/**
 * Базовий виняток порушення бізнес-правил предметної області.
 * У шарі веб-API (етап 4) перетворюється на відповідь {@code ProblemDetail}.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
