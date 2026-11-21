package tn.tekup.educated.vieetablissement.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessEventRepository extends JpaRepository<AccessEvent, UUID> {

    List<AccessEvent> findAllByOccurredAtBetweenOrderByOccurredAtDesc(Instant from, Instant to);
}
