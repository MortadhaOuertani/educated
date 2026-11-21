package tn.tekup.educated.scolarite.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import tn.tekup.educated.shared.domain.LocalizedText;
import tn.tekup.educated.shared.domain.TenantEntity;

/** A document learners can request, in one language. */
@Entity
@Table(name = "document_type", schema = "scolarite")
public class DocumentType extends TenantEntity {

    @Column(nullable = false, length = 40)
    private String code;

    @Embedded
    private LocalizedText label;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private DocumentLanguage language;

    private UUID templateFileId;

    private boolean active;

    public DocumentType() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalizedText getLabel() {
        return label;
    }

    public void setLabel(LocalizedText label) {
        this.label = label;
    }

    public DocumentLanguage getLanguage() {
        return language;
    }

    public void setLanguage(DocumentLanguage language) {
        this.language = language;
    }

    public UUID getTemplateFileId() {
        return templateFileId;
    }

    public void setTemplateFileId(UUID templateFileId) {
        this.templateFileId = templateFileId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
