package tn.tekup.educated.communication.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import tn.tekup.educated.shared.domain.TenantEntity;

/** A chat (staff internal, teacher-student or student-student). */
@Entity
@Table(name = "conversation", schema = "communication")
public class Conversation extends TenantEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ConversationType conversationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ConversationScope scope;

    private String title;

    private Instant lastMessageAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConversationParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt")
    private List<ChatMessage> messages = new ArrayList<>();

    public Conversation() {
    }

    public ConversationType getConversationType() {
        return conversationType;
    }

    public void setConversationType(ConversationType conversationType) {
        this.conversationType = conversationType;
    }

    public ConversationScope getScope() {
        return scope;
    }

    public void setScope(ConversationScope scope) {
        this.scope = scope;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Instant getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(Instant lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public List<ConversationParticipant> getParticipants() {
        return participants;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

}
