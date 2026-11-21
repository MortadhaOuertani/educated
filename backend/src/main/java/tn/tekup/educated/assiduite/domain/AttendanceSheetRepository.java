package tn.tekup.educated.assiduite.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceSheetRepository extends JpaRepository<AttendanceSheet, UUID> {

    Optional<AttendanceSheet> findBySessionOccurrenceId(UUID sessionOccurrenceId);
}
