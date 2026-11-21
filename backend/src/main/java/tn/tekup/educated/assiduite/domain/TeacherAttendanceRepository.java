package tn.tekup.educated.assiduite.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherAttendanceRepository extends JpaRepository<TeacherAttendance, UUID> {

    boolean existsBySessionOccurrenceId(UUID sessionOccurrenceId);

    List<TeacherAttendance> findAllByTeacherIdAndSessionDateBetweenOrderBySessionDate(UUID teacherId, LocalDate from,
            LocalDate to);

    List<TeacherAttendance> findAllByTeacherIdAndValidatedTrueAndSessionDateBetweenOrderBySessionDate(UUID teacherId,
            LocalDate from, LocalDate to);
}
