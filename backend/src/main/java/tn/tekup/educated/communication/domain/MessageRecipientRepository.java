package tn.tekup.educated.communication.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRecipientRepository extends JpaRepository<MessageRecipient, UUID> {

    List<MessageRecipient> findAllByRecipientUserIdAndArchivedFalseOrderByCreatedAtDesc(UUID recipientUserId);

    Optional<MessageRecipient> findByIdAndRecipientUserId(UUID id, UUID recipientUserId);
}
