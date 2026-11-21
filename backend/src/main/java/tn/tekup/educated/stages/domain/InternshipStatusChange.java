package tn.tekup.educated.stages.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import tn.tekup.educated.shared.domain.TenantEntity;

@Entity
@Table(name = "internship_status_change", schema = "stages")
public class InternshipStatusChange extends TenantEntity {

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private InternshipStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private InternshipStatus toStatus;

    @Column(length = 1000)
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "internship_id", nullable = false)
    private Internship internship;

    public InternshipStatusChange() {
    }

    public InternshipStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(InternshipStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public InternshipStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(InternshipStatus toStatus) {
        this.toStatus = toStatus;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Internship getInternship() {
        return internship;
    }

    public void setInternship(Internship internship) {
        this.internship = internship;
    }

}
