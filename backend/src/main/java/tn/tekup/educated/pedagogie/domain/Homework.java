package tn.tekup.educated.pedagogie.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** A homework (devoir). */
@Entity
@Table(name = "homework", schema = "pedagogie")
public class Homework extends TenantEntity {

    private UUID sessionOccurrenceId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private LocalDate assignedDate;

    private LocalDate dueDate;

    private UUID attachmentFileId;

    private boolean confirmed;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teaching_assignment_id", nullable = false)
    private TeachingAssignment teachingAssignment;

    public Homework() {
    }

    public UUID getSessionOccurrenceId() {
        return sessionOccurrenceId;
    }

    public void setSessionOccurrenceId(UUID sessionOccurrenceId) {
        this.sessionOccurrenceId = sessionOccurrenceId;
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

    public LocalDate getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(LocalDate assignedDate) {
        this.assignedDate = assignedDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public UUID getAttachmentFileId() {
        return attachmentFileId;
    }

    public void setAttachmentFileId(UUID attachmentFileId) {
        this.attachmentFileId = attachmentFileId;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public TeachingAssignment getTeachingAssignment() {
        return teachingAssignment;
    }

    public void setTeachingAssignment(TeachingAssignment teachingAssignment) {
        this.teachingAssignment = teachingAssignment;
    }

}
