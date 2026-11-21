package tn.tekup.educated.pedagogie.domain;

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

/** Fiche de charge of a teacher for an academic year. */
@Entity
@Table(name = "teacher_workload", schema = "pedagogie")
public class TeacherWorkload extends TenantEntity {

    @Column(nullable = false)
    private UUID academicYearId;

    private Integer statutoryHours;

    private Integer assignedHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private WorkloadStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    public TeacherWorkload() {
    }

    public UUID getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(UUID academicYearId) {
        this.academicYearId = academicYearId;
    }

    public Integer getStatutoryHours() {
        return statutoryHours;
    }

    public void setStatutoryHours(Integer statutoryHours) {
        this.statutoryHours = statutoryHours;
    }

    public Integer getAssignedHours() {
        return assignedHours;
    }

    public void setAssignedHours(Integer assignedHours) {
        this.assignedHours = assignedHours;
    }

    public WorkloadStatus getStatus() {
        return status;
    }

    public void setStatus(WorkloadStatus status) {
        this.status = status;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

}
