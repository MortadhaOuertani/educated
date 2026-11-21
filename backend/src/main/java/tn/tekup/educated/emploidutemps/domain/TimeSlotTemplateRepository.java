package tn.tekup.educated.emploidutemps.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeSlotTemplateRepository extends JpaRepository<TimeSlotTemplate, UUID> {

    Optional<TimeSlotTemplate> findByCode(String code);

    List<TimeSlotTemplate> findAllByOrderByOrderIndex();
}
