package tn.tekup.educated.pedagogie.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Enseignant par domaine. */
@Entity
@Table(name = "teacher_domain", schema = "pedagogie")
public class TeacherDomain extends TenantEntity {

    @Column(nullable = false)
    private UUID teachingDomainId;

    private boolean primaryDomain;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    public TeacherDomain() {
    }

    public UUID getTeachingDomainId() {
        return teachingDomainId;
    }

    public void setTeachingDomainId(UUID teachingDomainId) {
        this.teachingDomainId = teachingDomainId;
    }

    public boolean isPrimaryDomain() {
        return primaryDomain;
    }

    public void setPrimaryDomain(boolean primaryDomain) {
        this.primaryDomain = primaryDomain;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

}
