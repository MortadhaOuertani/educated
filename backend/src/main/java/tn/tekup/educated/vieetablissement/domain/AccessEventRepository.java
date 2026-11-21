package tn.tekup.educated.vieetablissement.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessEventRepository extends JpaRepository<AccessEvent, UUID> {
}
