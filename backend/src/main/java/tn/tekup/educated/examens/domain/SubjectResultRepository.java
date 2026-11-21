package tn.tekup.educated.examens.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectResultRepository extends JpaRepository<SubjectResult, UUID> {
}
