package tn.tekup.educated.stages.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InternshipRepository extends JpaRepository<Internship, UUID> {
}
