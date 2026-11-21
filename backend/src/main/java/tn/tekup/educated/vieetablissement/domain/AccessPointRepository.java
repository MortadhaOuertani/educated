package tn.tekup.educated.vieetablissement.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessPointRepository extends JpaRepository<AccessPoint, UUID> {

    Optional<AccessPoint> findByCode(String code);

    List<AccessPoint> findAllByOrderByCode();
}
