package tn.tekup.educated.assiduite.domain;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {

    List<AttendanceRecord> findAllByStudentIdAndSheet_Status(UUID studentId, AttendanceSheetStatus status);

    List<AttendanceRecord> findAllByStudentIdAndSheet_SessionOccurrenceIdIn(UUID studentId,
            Collection<UUID> sessionOccurrenceIds);
}
