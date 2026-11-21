package tn.tekup.educated.communication.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import tn.tekup.educated.shared.domain.Audience;
import tn.tekup.educated.shared.domain.TenantEntity;

/** A message to learners, guardians or teachers (app notification, SMS, e-mail). */
@Entity
@Table(name = "message", schema = "communication")
public class Message extends TenantEntity {

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "text")
    private String body;

    @Column(length = 160)
    private String smsText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Audience audience;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private MessageCategory category;

    private boolean sendPush;

    private boolean sendSms;

    private boolean sendEmail;

    private boolean alsoNotifyGuardians;

    private Instant sentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    private Message replyTo;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageAudienceFilter> filters = new ArrayList<>();

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageRecipient> recipients = new ArrayList<>();

    public Message() {
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSmsText() {
        return smsText;
    }

    public void setSmsText(String smsText) {
        this.smsText = smsText;
    }

    public Audience getAudience() {
        return audience;
    }

    public void setAudience(Audience audience) {
        this.audience = audience;
    }

    public MessageCategory getCategory() {
        return category;
    }

    public void setCategory(MessageCategory category) {
        this.category = category;
    }

    public boolean isSendPush() {
        return sendPush;
    }

    public void setSendPush(boolean sendPush) {
        this.sendPush = sendPush;
    }

    public boolean isSendSms() {
        return sendSms;
    }

    public void setSendSms(boolean sendSms) {
        this.sendSms = sendSms;
    }

    public boolean isSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    public boolean isAlsoNotifyGuardians() {
        return alsoNotifyGuardians;
    }

    public void setAlsoNotifyGuardians(boolean alsoNotifyGuardians) {
        this.alsoNotifyGuardians = alsoNotifyGuardians;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }

    public Message getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(Message replyTo) {
        this.replyTo = replyTo;
    }

    public List<MessageAttachment> getAttachments() {
        return attachments;
    }

    public List<MessageAudienceFilter> getFilters() {
        return filters;
    }

    public List<MessageRecipient> getRecipients() {
        return recipients;
    }

}
