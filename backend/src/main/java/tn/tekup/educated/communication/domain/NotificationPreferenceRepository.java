package tn.tekup.educated.communication.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {

    List<NotificationPreference> findAllByUserId(UUID userId);

    Optional<NotificationPreference> findByUserIdAndNotificationType(UUID userId, NotificationType type);
}
