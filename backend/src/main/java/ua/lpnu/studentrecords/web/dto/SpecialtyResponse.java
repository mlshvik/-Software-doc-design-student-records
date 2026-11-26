package ua.lpnu.studentrecords.web.dto;

/**
 * Спеціальність разом з факультетом.
 */
public record SpecialtyResponse(Long id,
                                String code,
                                String name,
                                Long facultyId,
                                String facultyShortName) {
}
