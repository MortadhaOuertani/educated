package tn.tekup.educated.scolarite.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KinshipRepository extends JpaRepository<Kinship, UUID> {

    List<Kinship> findAllByOrderByCode();

    Optional<Kinship> findByCode(String code);
}
