package tn.tekup.educated.stages.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InternshipNatureRepository extends JpaRepository<InternshipNature, UUID> {

    Optional<InternshipNature> findByCode(String code);

    List<InternshipNature> findAllByOrderByCode();
}
