package ua.lpnu.studentrecords.service.schedule;

import org.springframework.stereotype.Component;
import ua.lpnu.studentrecords.domain.entity.ScheduleEntry;
import ua.lpnu.studentrecords.domain.entity.TeachingAssignment;
import ua.lpnu.studentrecords.domain.enums.LessonType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Перевіряє нове (або змінене) заняття на конфлікти з наявним розкладом.
 * <p>
 * Два заняття перетинаються в часі, якщо збігаються день і номер пари, а типи тижнів
 * сумісні: «щотижня» перетинається з будь-яким, «чисельник» – лише з «чисельником»
 * ({@link ua.lpnu.studentrecords.domain.enums.WeekType#overlaps}). Для таких занять
 * перевіряється, чи не зайняті той самий викладач, аудиторія або група.
 * <p>
 * Виняток – потокова лекція: одна дисципліна, той самий викладач і аудиторія, тип «лекція»
 * для різних груп одночасно – це не конфлікт.
 */
@Component
public class ScheduleConflictChecker {

    public List<ScheduleConflict> findConflicts(ScheduleEntry candidate, Collection<ScheduleEntry> existingEntries) {
        List<ScheduleConflict> conflicts = new ArrayList<>();
        TeachingAssignment assignment = candidate.getAssignment();
        for (ScheduleEntry existing : existingEntries) {
            if (existing.isSameAs(candidate)
                    || !existing.overlapsInTime(candidate.getDayOfWeek(), candidate.getPairNumber(),
                    candidate.getWeekType())) {
                continue;
            }
            if (isStreamLecture(candidate, existing)) {
                continue;
            }
            TeachingAssignment other = existing.getAssignment();
            if (other.getTeacher().isSameAs(assignment.getTeacher())) {
                conflicts.add(new ScheduleConflict(ConflictType.TEACHER, existing,
                        "викладач " + other.getTeacher().getShortName() + " уже веде заняття " + describe(existing)));
            }
            if (existing.getClassroom().trim().equalsIgnoreCase(candidate.getClassroom().trim())) {
                conflicts.add(new ScheduleConflict(ConflictType.CLASSROOM, existing,
                        "аудиторія " + existing.getClassroom() + " уже зайнята: " + describe(existing)));
            }
            if (other.getGroup().isSameAs(assignment.getGroup())) {
                conflicts.add(new ScheduleConflict(ConflictType.GROUP, existing,
                        "група " + other.getGroup().getName() + " уже має заняття " + describe(existing)));
            }
        }
        return conflicts;
    }

    private static boolean isStreamLecture(ScheduleEntry a, ScheduleEntry b) {
        TeachingAssignment first = a.getAssignment();
        TeachingAssignment second = b.getAssignment();
        return a.getLessonType() == LessonType.LECTURE
                && b.getLessonType() == LessonType.LECTURE
                && first.getSubject().isSameAs(second.getSubject())
                && first.getTeacher().isSameAs(second.getTeacher())
                && !first.getGroup().isSameAs(second.getGroup())
                && a.getClassroom().trim().equalsIgnoreCase(b.getClassroom().trim());
    }

    private static String describe(ScheduleEntry entry) {
        TeachingAssignment assignment = entry.getAssignment();
        return "«" + assignment.getSubject().getName() + "» (група " + assignment.getGroup().getName()
                + ", ауд. " + entry.getClassroom() + ", " + entry.getDayOfWeek() + ", пара " + entry.getPairNumber() + ")";
    }
}
