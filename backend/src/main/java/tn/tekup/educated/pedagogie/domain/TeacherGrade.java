package tn.tekup.educated.pedagogie.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tn.tekup.educated.shared.domain.LocalizedText;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Grade of a teacher (Maître assistant, Ingénieur principal...). */
@Entity
@Table(name = "teacher_grade", schema = "pedagogie")
public class TeacherGrade extends TenantEntity {

    @Column(nullable = false, length = 20)
    private String code;

    @Embedded
    private LocalizedText label;

    public TeacherGrade() {
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
