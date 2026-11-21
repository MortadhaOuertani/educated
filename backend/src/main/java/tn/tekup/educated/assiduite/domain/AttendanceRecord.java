package tn.tekup.educated.assiduite.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Present / absent / late for one student in one session. */
@Entity
@Table(name = "attendance_record", schema = "assiduite")
public class AttendanceRecord extends TenantEntity {

    @Column(nullable = false)
    private UUID studentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AttendanceStatus status;

    private Integer lateMinutes;

    private boolean justified;

    @Column(length = 500)
    private String comment;

    private Instant guardiansNotifiedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sheet_id", nullable = false)
    private AttendanceSheet sheet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "justified_by_id")
    private StudentAbsenceRequest justifiedBy;

    public AttendanceRecord() {
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public Integer getLateMinutes() {
        return lateMinutes;
    }

    public void setLateMinutes(Integer lateMinutes) {
        this.lateMinutes = lateMinutes;
    }

    public boolean isJustified() {
        return justified;
    }

    public void setJustified(boolean justified) {
        this.justified = justified;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Instant getGuardiansNotifiedAt() {
        return guardiansNotifiedAt;
    }

    public void setGuardiansNotifiedAt(Instant guardiansNotifiedAt) {
        this.guardiansNotifiedAt = guardiansNotifiedAt;
    }

    public AttendanceSheet getSheet() {
        return sheet;
    }

    public void setSheet(AttendanceSheet sheet) {
        this.sheet = sheet;
    }

    public StudentAbsenceRequest getJustifiedBy() {
        return justifiedBy;
    }

    public void setJustifiedBy(StudentAbsenceRequest justifiedBy) {
        this.justifiedBy = justifiedBy;
    }

}
