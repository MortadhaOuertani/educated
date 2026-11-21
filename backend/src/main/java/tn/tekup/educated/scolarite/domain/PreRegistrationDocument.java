package tn.tekup.educated.scolarite.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

@Entity
@Table(name = "pre_registration_document", schema = "scolarite")
public class PreRegistrationDocument extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String label;

    @Column(nullable = false)
    private UUID fileId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private PreRegistrationRequest request;

    public PreRegistrationDocument() {
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public UUID getFileId() {
        return fileId;
    }

    public void setFileId(UUID fileId) {
        this.fileId = fileId;
    }

    public PreRegistrationRequest getRequest() {
        return request;
    }

    public void setRequest(PreRegistrationRequest request) {
        this.request = request;
    }

}
