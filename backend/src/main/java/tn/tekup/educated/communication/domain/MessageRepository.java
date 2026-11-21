package tn.tekup.educated.communication.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findAllByCreatedByUserIdOrderByCreatedAtDesc(UUID userId);
}
