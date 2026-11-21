package tn.tekup.educated.stages.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DefenseRepository extends JpaRepository<Defense, UUID> {

    Optional<Defense> findByInternship_Id(UUID internshipId);

    List<Defense> findAllByJury_IdOrderByStartTime(UUID juryId);

    List<Defense> findAllByInternship_StudentIdAndPublishedTrue(UUID studentId);
}
