package tn.tekup.educated.emploidutemps.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionOccurrenceRepository extends JpaRepository<SessionOccurrence, UUID> {

    List<SessionOccurrence> findAllBySchoolClassIdAndSessionDateBetweenOrderBySessionDateAscStartTimeAsc(
            UUID schoolClassId, LocalDate from, LocalDate to);

    List<SessionOccurrence> findAllByTeacherIdAndSessionDateBetweenOrderBySessionDateAscStartTimeAsc(
            UUID teacherId, LocalDate from, LocalDate to);

    List<SessionOccurrence> findAllBySlot_Timetable_IdAndStatusAndSessionDateGreaterThanEqual(UUID timetableId,
            OccurrenceStatus status, LocalDate from);
}
