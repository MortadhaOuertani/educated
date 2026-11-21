package tn.tekup.educated.examens.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeGridRepository extends JpaRepository<GradeGrid, UUID> {
}
