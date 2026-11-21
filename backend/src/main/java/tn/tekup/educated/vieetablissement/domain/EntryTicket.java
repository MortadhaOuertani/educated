package tn.tekup.educated.vieetablissement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Billet d'entrée after a late arrival or an absence. */
@Entity
@Table(name = "entry_ticket", schema = "vieetablissement")
public class EntryTicket extends TenantEntity {

    @Column(nullable = false)
    private UUID studentId;

    private UUID attendanceRecordId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EntryTicketReason reason;

    @Column(nullable = false)
    private LocalDate validForDate;

    @Column(length = 500)
    private String note;

    public EntryTicket() {
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public UUID getAttendanceRecordId() {
        return attendanceRecordId;
    }

    public void setAttendanceRecordId(UUID attendanceRecordId) {
        this.attendanceRecordId = attendanceRecordId;
    }

    public EntryTicketReason getReason() {
        return reason;
    }

    public void setReason(EntryTicketReason reason) {
        this.reason = reason;
    }

    public LocalDate getValidForDate() {
        return validForDate;
    }

    public void setValidForDate(LocalDate validForDate) {
        this.validForDate = validForDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

}
