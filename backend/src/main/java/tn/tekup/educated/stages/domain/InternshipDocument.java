package tn.tekup.educated.stages.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Report, certificate or agreement of an internship. */
@Entity
@Table(name = "internship_document", schema = "stages")
public class InternshipDocument extends TenantEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private InternshipDocumentType documentType;

    @Column(nullable = false)
    private UUID fileId;

    private boolean locked;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "internship_id", nullable = false)
    private Internship internship;

    public InternshipDocument() {
    }

    public InternshipDocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(InternshipDocumentType documentType) {
        this.documentType = documentType;
    }

    public UUID getFileId() {
        return fileId;
    }

    public void setFileId(UUID fileId) {
        this.fileId = fileId;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public Internship getInternship() {
        return internship;
    }

    public void setInternship(Internship internship) {
        this.internship = internship;
    }

}
