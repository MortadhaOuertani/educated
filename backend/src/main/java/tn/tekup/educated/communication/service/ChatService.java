package tn.tekup.educated.communication.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tekup.educated.administration.api.AccountDirectory;
import tn.tekup.educated.administration.api.AccountDirectory.AccountInfo;
import tn.tekup.educated.communication.domain.ChatMessage;
import tn.tekup.educated.communication.domain.ChatMessageRepository;
import tn.tekup.educated.communication.domain.Conversation;
import tn.tekup.educated.communication.domain.ConversationParticipant;
import tn.tekup.educated.communication.domain.ConversationRepository;
import tn.tekup.educated.communication.domain.ConversationScope;
import tn.tekup.educated.communication.domain.ConversationType;
import tn.tekup.educated.communication.domain.NotificationType;
import tn.tekup.educated.shared.domain.PersonType;
import tn.tekup.educated.shared.web.BusinessRuleException;
import tn.tekup.educated.shared.web.NotFoundException;

/** Discussions between two people (staff, teacher-student or student-student). */
@Service
@Transactional
public class ChatService {

    private final ConversationRepository conversations;
    private final ChatMessageRepository chatMessages;
    private final AccountDirectory accounts;
    private final NotificationService notifications;

    public ChatService(ConversationRepository conversations, ChatMessageRepository chatMessages,
            AccountDirectory accounts, NotificationService notifications) {
        this.conversations = conversations;
        this.chatMessages = chatMessages;
        this.accounts = accounts;
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public List<Conversation> conversationsOf(UUID userId) {
        return conversations.findAllWithParticipant(userId);
    }

    /** Returns the existing direct conversation between the two users, or starts one. */
    public Conversation directConversation(UUID me, UUID other) {
        if (me.equals(other)) {
            throw new BusinessRuleException("chat.self", "You cannot start a discussion with yourself.");
        }
        AccountInfo myAccount = accounts.findAccount(me).orElseThrow(() -> new NotFoundException("UserAccount", me));
        AccountInfo otherAccount = accounts.findAccount(other).orElseThrow(() -> new NotFoundException("UserAccount", other));
        return conversations.findDirect(me, other).stream().findFirst().orElseGet(() -> {
            Conversation conversation = new Conversation();
            conversation.setConversationType(ConversationType.DIRECT);
            conversation.setScope(scope(myAccount.personType(), otherAccount.personType()));
            conversation.setTitle(otherAccount.username());
            for (UUID userId : List.of(me, other)) {
                ConversationParticipant participant = new ConversationParticipant();
                participant.setConversation(conversation);
                participant.setUserId(userId);
                conversation.getParticipants().add(participant);
            }
            return conversations.save(conversation);
        });
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> messages(UUID conversationId, UUID userId) {
        participantConversation(conversationId, userId);
        return chatMessages.findAllByConversation_IdOrderByCreatedAt(conversationId);
    }

    public ChatMessage send(UUID conversationId, UUID userId, String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessRuleException("chat.empty", "The message is empty.");
        }
        Conversation conversation = participantConversation(conversationId, userId);
        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setContent(content.trim());
        conversation.getMessages().add(message);
        Instant now = Instant.now();
        conversation.setLastMessageAt(now);
        List<UUID> others = conversation.getParticipants().stream().map(ConversationParticipant::getUserId)
                .filter(id -> !id.equals(userId)).toList();
        conversation.getParticipants().stream().filter(p -> p.getUserId().equals(userId))
                .forEach(p -> p.setLastReadAt(now));
        message = chatMessages.save(message);
        notifications.notifyUsers(others, NotificationType.DISCUSSION, "Nouveau message", content.length() > 80
                ? content.substring(0, 80) + "…" : content, "Conversation", conversationId);
        return message;
    }

    private Conversation participantConversation(UUID conversationId, UUID userId) {
        return conversations.findById(conversationId)
                .filter(c -> c.getParticipants().stream().anyMatch(p -> p.getUserId().equals(userId)))
                .orElseThrow(() -> new NotFoundException("Conversation", conversationId));
    }

    private static ConversationScope scope(PersonType a, PersonType b) {
        if (a == PersonType.STUDENT && b == PersonType.STUDENT) {
            return ConversationScope.STUDENT_STUDENT;
        }
        if (a == PersonType.STUDENT || b == PersonType.STUDENT) {
            return ConversationScope.TEACHER_STUDENT;
        }
        return ConversationScope.STAFF_INTERNAL;
    }
}
