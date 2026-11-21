package tn.tekup.educated.assiduite.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Présence des enseignants: hours taught in one session. */
@Entity
@Table(name = "teacher_attendance", schema = "assiduite")
public class TeacherAttendance extends TenantEntity {

    @Column(nullable = false)
    private UUID sessionOccurrenceId;

    @Column(nullable = false)
    private UUID teacherId;

    @Column(nullable = false)
    private LocalDate sessionDate;

    private Integer weekNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TeacherPresenceStatus status;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal hours;

    private boolean validated;

    private UUID validatedByUserId;

    private Instant validatedAt;

    public TeacherAttendance() {
    }

    public UUID getSessionOccurrenceId() {
        return sessionOccurrenceId;
    }

    public void setSessionOccurrenceId(UUID sessionOccurrenceId) {
        this.sessionOccurrenceId = sessionOccurrenceId;
    }

    public UUID getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(UUID teacherId) {
        this.teacherId = teacherId;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public Integer getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(Integer weekNumber) {
        this.weekNumber = weekNumber;
    }

    public TeacherPresenceStatus getStatus() {
        return status;
    }

    public void setStatus(TeacherPresenceStatus status) {
        this.status = status;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public void setHours(BigDecimal hours) {
        this.hours = hours;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public UUID getValidatedByUserId() {
        return validatedByUserId;
    }

    public void setValidatedByUserId(UUID validatedByUserId) {
        this.validatedByUserId = validatedByUserId;
    }

    public Instant getValidatedAt() {
        return validatedAt;
    }

    public void setValidatedAt(Instant validatedAt) {
        this.validatedAt = validatedAt;
    }

}
