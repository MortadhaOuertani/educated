package tn.tekup.educated.parametrage.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecializationRepository extends JpaRepository<Specialization, UUID> {

    List<Specialization> findAllByOrderByCode();
}
