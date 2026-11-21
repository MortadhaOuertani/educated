package tn.tekup.educated.communication.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News, UUID> {
}
