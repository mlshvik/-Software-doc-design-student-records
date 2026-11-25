package ua.lpnu.studentrecords.web.dto;

import java.time.DayOfWeek;
import ua.lpnu.studentrecords.domain.enums.LessonType;
import ua.lpnu.studentrecords.domain.enums.WeekType;

/**
 * Заняття в розкладі з дисципліною, викладачем і групою.
 */
public record ScheduleEntryResponse(Long id,
                                    Long assignmentId,
                                    DayOfWeek dayOfWeek,
                                    Integer pairNumber,
                                    WeekType weekType,
                                    String classroom,
                                    LessonType lessonType,
                                    String subjectName,
                                    Long teacherId,
                                    String teacherName,
                                    Long groupId,
                                    String groupName) {
}
