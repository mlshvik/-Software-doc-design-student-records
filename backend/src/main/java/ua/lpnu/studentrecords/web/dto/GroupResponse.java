package ua.lpnu.studentrecords.web.dto;

/**
 * Академічна група зі спеціальністю і факультетом.
 */
public record GroupResponse(Long id,
                            String name,
                            Integer course,
                            Integer admissionYear,
                            Long specialtyId,
                            String specialtyCode,
                            String specialtyName,
                            Long facultyId,
                            String facultyShortName) {
}
