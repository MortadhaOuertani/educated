package tn.tekup.educated.communication.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tn.tekup.educated.shared.domain.Audience;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Actualité published to the apps. */
@Entity
@Table(name = "news", schema = "communication")
public class News extends TenantEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String body;

    private UUID coverImageFileId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Audience audience;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NewsStatus status;

    private UUID publishedByUserId;

    private Instant publishedAt;

    private Instant notifiedAt;

    @OneToMany(mappedBy = "news", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NewsAttachment> attachments = new ArrayList<>();

    public News() {
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

    public UUID getCoverImageFileId() {
        return coverImageFileId;
    }

    public void setCoverImageFileId(UUID coverImageFileId) {
        this.coverImageFileId = coverImageFileId;
    }

    public Audience getAudience() {
        return audience;
    }

    public void setAudience(Audience audience) {
        this.audience = audience;
    }

    public NewsStatus getStatus() {
        return status;
    }

    public void setStatus(NewsStatus status) {
        this.status = status;
    }

    public UUID getPublishedByUserId() {
        return publishedByUserId;
    }

    public void setPublishedByUserId(UUID publishedByUserId) {
        this.publishedByUserId = publishedByUserId;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Instant getNotifiedAt() {
        return notifiedAt;
    }

    public void setNotifiedAt(Instant notifiedAt) {
        this.notifiedAt = notifiedAt;
    }

    public List<NewsAttachment> getAttachments() {
        return attachments;
    }

}
