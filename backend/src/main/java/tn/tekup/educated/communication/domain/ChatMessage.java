package tn.tekup.educated.communication.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

@Entity
@Table(name = "chat_message", schema = "communication")
public class ChatMessage extends TenantEntity {

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    private UUID attachmentFileId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    public ChatMessage() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UUID getAttachmentFileId() {
        return attachmentFileId;
    }

    public void setAttachmentFileId(UUID attachmentFileId) {
        this.attachmentFileId = attachmentFileId;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

}
