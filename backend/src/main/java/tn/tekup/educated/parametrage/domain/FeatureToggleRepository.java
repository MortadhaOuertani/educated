package tn.tekup.educated.parametrage.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeatureToggleRepository extends JpaRepository<FeatureToggle, UUID> {

    List<FeatureToggle> findAllByPortal(Portal portal);

    Optional<FeatureToggle> findByFeatureKeyAndPortal(String featureKey, Portal portal);
}
