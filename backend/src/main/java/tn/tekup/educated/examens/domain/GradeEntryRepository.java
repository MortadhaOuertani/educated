package tn.tekup.educated.examens.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeEntryRepository extends JpaRepository<GradeEntry, UUID> {

    List<GradeEntry> findAllByStudentIdAndGrid_StatusOrderByGrid_PublishedAtDesc(UUID studentId,
            GradeGridStatus status);
}
