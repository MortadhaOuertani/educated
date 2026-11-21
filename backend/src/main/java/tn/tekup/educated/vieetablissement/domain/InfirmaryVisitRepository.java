package tn.tekup.educated.vieetablissement.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InfirmaryVisitRepository extends JpaRepository<InfirmaryVisit, UUID> {
}
