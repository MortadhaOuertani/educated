package tn.tekup.educated.scolarite.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Demande de document (Déposée → En traitement → Traitée). */
@Entity
@Table(name = "document_request", schema = "scolarite")
public class DocumentRequest extends TenantEntity {

    @Column(length = 1000)
    private String reason;

    private UUID proofFileId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private DocumentRequestStatus status;

    private UUID processedByUserId;

    private Instant processedAt;

    private UUID generatedFileId;

    private Instant collectedAt;

    @Column(length = 1000)
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_type_id", nullable = false)
    private DocumentType documentType;

    public DocumentRequest() {
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public UUID getProofFileId() {
        return proofFileId;
    }

    public void setProofFileId(UUID proofFileId) {
        this.proofFileId = proofFileId;
    }

    public DocumentRequestStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentRequestStatus status) {
        this.status = status;
    }

    public UUID getProcessedByUserId() {
        return processedByUserId;
    }

    public void setProcessedByUserId(UUID processedByUserId) {
        this.processedByUserId = processedByUserId;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public UUID getGeneratedFileId() {
        return generatedFileId;
    }

    public void setGeneratedFileId(UUID generatedFileId) {
        this.generatedFileId = generatedFileId;
    }

    public Instant getCollectedAt() {
        return collectedAt;
    }

    public void setCollectedAt(Instant collectedAt) {
        this.collectedAt = collectedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

}
