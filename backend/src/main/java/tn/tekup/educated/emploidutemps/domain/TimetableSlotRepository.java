package tn.tekup.educated.emploidutemps.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TimetableSlotRepository extends JpaRepository<TimetableSlot, UUID> {

    /** Slots of the period's active timetables that overlap the given time on that day. */
    @Query("""
            select s from TimetableSlot s
            where s.timetable.academicPeriodId = :periodId
              and s.timetable.status <> tn.tekup.educated.emploidutemps.domain.TimetableStatus.ARCHIVED
              and s.dayOfWeek = :day and s.startTime < :endTime and s.endTime > :startTime
            """)
    List<TimetableSlot> findOverlapping(@Param("periodId") UUID periodId, @Param("day") DayOfWeek day,
            @Param("startTime") LocalTime startTime, @Param("endTime") LocalTime endTime);
}
