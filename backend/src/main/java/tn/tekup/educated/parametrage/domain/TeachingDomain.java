package tn.tekup.educated.parametrage.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tn.tekup.educated.shared.domain.LocalizedText;
import tn.tekup.educated.shared.domain.TenantEntity;

/** A teaching domain (Enseignant par domaine). */
@Entity
@Table(name = "teaching_domain", schema = "parametrage")
public class TeachingDomain extends TenantEntity {

    @Column(nullable = false, length = 20)
    private String code;

    @Embedded
    private LocalizedText name;

    public TeachingDomain() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalizedText getName() {
        return name;
    }

    public void setName(LocalizedText name) {
        this.name = name;
    }

}
