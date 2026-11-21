package tn.tekup.educated.communication.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** A notification shown in the bell and pushed to the mobile app. */
@Entity
@Table(name = "notification", schema = "communication")
public class Notification extends TenantEntity {

    @Column(nullable = false)
    private UUID recipientUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType notificationType;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String body;

    @Column(length = 80)
    private String targetType;

    private UUID targetId;

    private Instant readAt;

    private Instant pushSentAt;

    private Instant smsSentAt;

    public Notification() {
    }

    public UUID getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(UUID recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public void setReadAt(Instant readAt) {
        this.readAt = readAt;
    }

    public Instant getPushSentAt() {
        return pushSentAt;
    }

    public void setPushSentAt(Instant pushSentAt) {
        this.pushSentAt = pushSentAt;
    }

    public Instant getSmsSentAt() {
        return smsSentAt;
    }

    public void setSmsSentAt(Instant smsSentAt) {
        this.smsSentAt = smsSentAt;
    }

}
