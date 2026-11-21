package tn.tekup.educated.parametrage.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstablishmentRepository extends JpaRepository<Establishment, UUID> {

    Optional<Establishment> findByCode(String code);

    List<Establishment> findAllByActiveTrueOrderByCode();
}
