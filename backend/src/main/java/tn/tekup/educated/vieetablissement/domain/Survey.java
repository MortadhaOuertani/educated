package tn.tekup.educated.vieetablissement.domain;

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
import java.util.UUID;
import tn.tekup.educated.shared.domain.Audience;
import tn.tekup.educated.shared.domain.TenantEntity;

/** A survey (sondage), including course evaluations. */
@Entity
@Table(name = "survey", schema = "vieetablissement")
public class Survey extends TenantEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SurveyPurpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Audience audience;

    private UUID teachingAssignmentId;

    private Instant opensAt;

    private Instant closesAt;

    private boolean anonymous;

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex")
    private List<SurveyQuestion> questions = new ArrayList<>();

    public Survey() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SurveyPurpose getPurpose() {
        return purpose;
    }

    public void setPurpose(SurveyPurpose purpose) {
        this.purpose = purpose;
    }

    public Audience getAudience() {
        return audience;
    }

    public void setAudience(Audience audience) {
        this.audience = audience;
    }

    public UUID getTeachingAssignmentId() {
        return teachingAssignmentId;
    }

    public void setTeachingAssignmentId(UUID teachingAssignmentId) {
        this.teachingAssignmentId = teachingAssignmentId;
    }

    public Instant getOpensAt() {
        return opensAt;
    }

    public void setOpensAt(Instant opensAt) {
        this.opensAt = opensAt;
    }

    public Instant getClosesAt() {
        return closesAt;
    }

    public void setClosesAt(Instant closesAt) {
        this.closesAt = closesAt;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public List<SurveyQuestion> getQuestions() {
        return questions;
    }

}
