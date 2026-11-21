package tn.tekup.educated.pedagogie.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Cahier de texte entry for one session. */
@Entity
@Table(name = "course_journal_entry", schema = "pedagogie")
public class CourseJournalEntry extends TenantEntity {

    @Column(nullable = false)
    private UUID sessionOccurrenceId;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    private boolean confirmed;

    private UUID confirmedByUserId;

    private Instant confirmedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teaching_assignment_id", nullable = false)
    private TeachingAssignment teachingAssignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private ProgramChapter chapter;

    public CourseJournalEntry() {
    }

    public UUID getSessionOccurrenceId() {
        return sessionOccurrenceId;
    }

    public void setSessionOccurrenceId(UUID sessionOccurrenceId) {
        this.sessionOccurrenceId = sessionOccurrenceId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public UUID getConfirmedByUserId() {
        return confirmedByUserId;
    }

    public void setConfirmedByUserId(UUID confirmedByUserId) {
        this.confirmedByUserId = confirmedByUserId;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Instant confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public TeachingAssignment getTeachingAssignment() {
        return teachingAssignment;
    }

    public void setTeachingAssignment(TeachingAssignment teachingAssignment) {
        this.teachingAssignment = teachingAssignment;
    }

    public ProgramChapter getChapter() {
        return chapter;
    }

    public void setChapter(ProgramChapter chapter) {
        this.chapter = chapter;
    }

}
