package tn.tekup.educated.examens.domain;

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

/** Surveillance duty of a teacher. */
@Entity
@Table(name = "invigilation", schema = "examens")
public class Invigilation extends TenantEntity {

    @Column(nullable = false)
    private UUID teacherId;

    private UUID roomId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private InvigilationRole role;

    private boolean published;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    public Invigilation() {
    }

    public UUID getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(UUID teacherId) {
        this.teacherId = teacherId;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    public InvigilationRole getRole() {
        return role;
    }

    public void setRole(InvigilationRole role) {
        this.role = role;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

}
