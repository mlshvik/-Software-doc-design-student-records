package ua.lpnu.studentrecords.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.lpnu.studentrecords.domain.entity.ScheduleEntry;

import java.time.DayOfWeek;
import java.util.List;

public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry, Long> {

    @Query("select e from ScheduleEntry e join fetch e.assignment a join fetch a.subject " +
           "join fetch a.teacher join fetch a.group where a.group.id = :groupId")
    List<ScheduleEntry> findByGroupId(@Param("groupId") Long groupId);

    @Query("select e from ScheduleEntry e join fetch e.assignment a join fetch a.subject " +
           "join fetch a.teacher join fetch a.group where a.teacher.id = :teacherId")
    List<ScheduleEntry> findByTeacherId(@Param("teacherId") Long teacherId);

    /** Заняття в конкретну пару конкретного дня (для перевірки конфліктів розкладу). */
    @Query("select e from ScheduleEntry e join fetch e.assignment a join fetch a.subject " +
           "join fetch a.teacher join fetch a.group where e.dayOfWeek = :day and e.pairNumber = :pair")
    List<ScheduleEntry> findAtTime(@Param("day") DayOfWeek day, @Param("pair") Integer pair);
}
