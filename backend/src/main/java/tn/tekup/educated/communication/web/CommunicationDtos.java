package tn.tekup.educated.communication.web;

import java.time.Instant;
import java.util.UUID;
import tn.tekup.educated.communication.domain.ChatMessage;
import tn.tekup.educated.communication.domain.Conversation;
import tn.tekup.educated.communication.domain.Message;
import tn.tekup.educated.communication.domain.MessageRecipient;
import tn.tekup.educated.communication.domain.News;
import tn.tekup.educated.communication.domain.Notification;
import tn.tekup.educated.communication.domain.NotificationPreference;

/** JSON views of the communication module. */
final class CommunicationDtos {

    private CommunicationDtos() {
    }

    record NotificationDto(UUID id, String type, String title, String body, String targetType, UUID targetId,
            Instant createdAt, Instant readAt) {
        static NotificationDto from(Notification n) {
            return new NotificationDto(n.getId(), n.getNotificationType().name(), n.getTitle(), n.getBody(),
                    n.getTargetType(), n.getTargetId(), n.getCreatedAt(), n.getReadAt());
        }
    }

    record UnreadDto(long count) {
    }

    record PreferenceDto(String type, boolean inApp, boolean push, boolean email, boolean sms) {
        static PreferenceDto from(NotificationPreference p) {
            return new PreferenceDto(p.getNotificationType().name(), p.isInApp(), p.isPush(), p.isEmail(), p.isSms());
        }
    }

    record SentMessageDto(UUID id, String subject, String body, String audience, String category, Instant sentAt,
            int recipients) {
        static SentMessageDto from(Message m) {
            return new SentMessageDto(m.getId(), m.getSubject(), m.getBody(), m.getAudience().name(),
                    m.getCategory().name(), m.getSentAt(), m.getRecipients().size());
        }
    }

    record InboxDto(UUID id, UUID messageId, String subject, String body, String category, Instant sentAt,
            Instant readAt) {
        static InboxDto from(MessageRecipient r) {
            Message m = r.getMessage();
            return new InboxDto(r.getId(), m.getId(), m.getSubject(), m.getBody(), m.getCategory().name(),
                    m.getSentAt(), r.getReadAt());
        }
    }

    record NewsDto(UUID id, String title, String body, String audience, String status, Instant publishedAt) {
        static NewsDto from(News n) {
            return new NewsDto(n.getId(), n.getTitle(), n.getBody(), n.getAudience().name(), n.getStatus().name(),
                    n.getPublishedAt());
        }
    }

    record ConversationDto(UUID id, String title, String scope, Instant lastMessageAt) {
        static ConversationDto from(Conversation c) {
            return new ConversationDto(c.getId(), c.getTitle(), c.getScope().name(), c.getLastMessageAt());
        }
    }

    record ChatMessageDto(UUID id, UUID senderUserId, String content, Instant sentAt) {
        static ChatMessageDto from(ChatMessage m) {
            return new ChatMessageDto(m.getId(), m.getCreatedByUserId(), m.getContent(), m.getCreatedAt());
        }
    }
}
