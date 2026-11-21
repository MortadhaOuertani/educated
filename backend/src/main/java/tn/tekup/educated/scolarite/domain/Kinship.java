package tn.tekup.educated.scolarite.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tn.tekup.educated.shared.domain.LocalizedText;
import tn.tekup.educated.shared.domain.TenantEntity;

/** A relationship type (Père, Mère, Tuteur...). */
@Entity
@Table(name = "kinship", schema = "scolarite")
public class Kinship extends TenantEntity {

    @Column(nullable = false, length = 20)
    private String code;

    @Embedded
    private LocalizedText label;

    public Kinship() {
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

}
