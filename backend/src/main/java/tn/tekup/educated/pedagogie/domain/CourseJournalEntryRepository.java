package tn.tekup.educated.pedagogie.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseJournalEntryRepository extends JpaRepository<CourseJournalEntry, UUID> {

    List<CourseJournalEntry> findAllByTeachingAssignment_IdOrderByCreatedAtDesc(UUID teachingAssignmentId);

    boolean existsBySessionOccurrenceId(UUID sessionOccurrenceId);
}
