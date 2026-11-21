package tn.tekup.educated.assiduite.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Fiche de présence of one session. */
@Entity
@Table(name = "attendance_sheet", schema = "assiduite")
public class AttendanceSheet extends TenantEntity {

    @Column(nullable = false)
    private UUID sessionOccurrenceId;

    @Column(nullable = false)
    private UUID schoolClassId;

    private UUID teacherId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AttendanceSheetStatus status;

    private Instant confirmedAt;

    private Instant editableUntil;

    @OneToMany(mappedBy = "sheet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttendanceRecord> records = new ArrayList<>();

    public AttendanceSheet() {
    }

    public UUID getSessionOccurrenceId() {
        return sessionOccurrenceId;
    }

    public void setSessionOccurrenceId(UUID sessionOccurrenceId) {
        this.sessionOccurrenceId = sessionOccurrenceId;
    }

    public UUID getSchoolClassId() {
        return schoolClassId;
    }

    public void setSchoolClassId(UUID schoolClassId) {
        this.schoolClassId = schoolClassId;
    }

    public UUID getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(UUID teacherId) {
        this.teacherId = teacherId;
    }

    public AttendanceSheetStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceSheetStatus status) {
        this.status = status;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Instant confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public Instant getEditableUntil() {
        return editableUntil;
    }

    public void setEditableUntil(Instant editableUntil) {
        this.editableUntil = editableUntil;
    }

    public List<AttendanceRecord> getRecords() {
        return records;
    }

}
