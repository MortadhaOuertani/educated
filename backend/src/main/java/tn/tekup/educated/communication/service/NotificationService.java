package tn.tekup.educated.communication.service;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.administration.api.AccountDirectory;
import tn.tekup.educated.communication.domain.Notification;
import tn.tekup.educated.communication.domain.NotificationPreference;
import tn.tekup.educated.communication.domain.NotificationPreferenceRepository;
import tn.tekup.educated.communication.domain.NotificationRepository;
import tn.tekup.educated.communication.domain.NotificationType;
import tn.tekup.educated.shared.domain.PersonType;
import tn.tekup.educated.shared.web.NotFoundException;

/** The notification bell: one row per recipient, unless they switched that kind of notification off. */
@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notifications;
    private final NotificationPreferenceRepository preferences;
    private final AccountDirectory accounts;

    public NotificationService(NotificationRepository notifications, NotificationPreferenceRepository preferences,
            AccountDirectory accounts) {
        this.notifications = notifications;
        this.preferences = preferences;
        this.accounts = accounts;
    }

    public int notifyUsers(Collection<UUID> userIds, NotificationType type, String title, String body,
            String targetType, UUID targetId) {
        int sent = 0;
        for (UUID userId : new LinkedHashSet<>(userIds)) {
            boolean wanted = preferences.findByUserIdAndNotificationType(userId, type)
                    .map(NotificationPreference::isInApp).orElse(true);
            if (!wanted) {
                continue;
            }
            Notification notification = new Notification();
            notification.setRecipientUserId(userId);
            notification.setNotificationType(type);
            notification.setTitle(title);
            notification.setBody(body);
            notification.setTargetType(targetType);
            notification.setTargetId(targetId);
            notifications.save(notification);
            sent++;
        }
        return sent;
    }

    /** Notifies the accounts of the given students or teachers. */
    public int notifyPeople(PersonType type, Collection<UUID> personIds, NotificationType notificationType,
            String title, String body, String targetType, UUID targetId) {
        return notifyUsers(accounts.userIdsOf(type, personIds), notificationType, title, body, targetType, targetId);
    }

    /**
     * Same as {@link #notifyPeople}, in a transaction of its own. Event listeners use it: they run after the other
     * module's transaction has committed, so they cannot write in that transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int notifyPeopleAfterCommit(PersonType type, Collection<UUID> personIds, NotificationType notificationType,
            String title, String body, String targetType, UUID targetId) {
        return notifyPeople(type, personIds, notificationType, title, body, targetType, targetId);
    }

    @Transactional(readOnly = true)
    public List<Notification> latest(UUID userId) {
        return notifications.findAllByRecipientUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 50));
    }

    @Transactional(readOnly = true)
    public long unreadCount(UUID userId) {
        return notifications.countByRecipientUserIdAndReadAtIsNull(userId);
    }

    public Notification markRead(UUID id, UUID userId) {
        Notification notification = notifications.findById(id)
                .filter(n -> n.getRecipientUserId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Notification", id));
        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());
        }
        return notification;
    }

    public void markAllRead(UUID userId) {
        Instant now = Instant.now();
        notifications.findAllByRecipientUserIdAndReadAtIsNull(userId).forEach(n -> n.setReadAt(now));
    }

    @Transactional(readOnly = true)
    public List<NotificationPreference> preferencesOf(UUID userId) {
        return preferences.findAllByUserId(userId);
    }

    public NotificationPreference setPreference(UUID userId, NotificationType type, boolean inApp, boolean push,
            boolean email, boolean sms) {
        NotificationPreference preference = preferences.findByUserIdAndNotificationType(userId, type).orElseGet(() -> {
            NotificationPreference created = new NotificationPreference();
            created.setUserId(userId);
            created.setNotificationType(type);
            return created;
        });
        preference.setInApp(inApp);
        preference.setPush(push);
        preference.setEmail(email);
        preference.setSms(sms);
        return preferences.save(preference);
    }
}
