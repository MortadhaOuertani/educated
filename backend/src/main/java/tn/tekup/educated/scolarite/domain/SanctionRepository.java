package tn.tekup.educated.scolarite.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SanctionRepository extends JpaRepository<Sanction, UUID> {

    List<Sanction> findAllByStudent_IdOrderByIncidentDateDesc(UUID studentId);
}
