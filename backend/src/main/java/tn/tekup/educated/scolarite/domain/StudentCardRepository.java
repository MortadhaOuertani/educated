package tn.tekup.educated.scolarite.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentCardRepository extends JpaRepository<StudentCard, UUID> {
}
