package tn.tekup.educated.scolarite.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiblingRelationRepository extends JpaRepository<SiblingRelation, UUID> {
}
