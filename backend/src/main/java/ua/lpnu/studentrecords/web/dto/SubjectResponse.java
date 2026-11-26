package ua.lpnu.studentrecords.web.dto;

import java.math.BigDecimal;
import ua.lpnu.studentrecords.domain.enums.ControlType;

/**
 * Навчальна дисципліна.
 */
public record SubjectResponse(Long id,
                              String name,
                              BigDecimal credits,
                              Integer hours,
                              ControlType controlType) {
}
