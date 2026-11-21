package tn.tekup.educated.pedagogie.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

@Entity
@Table(name = "training_participation", schema = "pedagogie")
public class TrainingParticipation extends TenantEntity {

    private boolean attended;

    private UUID certificateFileId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_id", nullable = false)
    private ContinuousTraining training;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    public TrainingParticipation() {
    }

    public boolean isAttended() {
        return attended;
    }

    public void setAttended(boolean attended) {
        this.attended = attended;
    }

    public UUID getCertificateFileId() {
        return certificateFileId;
    }

    public void setCertificateFileId(UUID certificateFileId) {
        this.certificateFileId = certificateFileId;
    }

    public ContinuousTraining getTraining() {
        return training;
    }

    public void setTraining(ContinuousTraining training) {
        this.training = training;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

}
