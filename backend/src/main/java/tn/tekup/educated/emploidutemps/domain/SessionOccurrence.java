package tn.tekup.educated.emploidutemps.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import tn.tekup.educated.shared.domain.SessionType;
import tn.tekup.educated.shared.domain.TenantEntity;

/** An actual dated session (Programme des séances). */
@Entity
@Table(name = "session_occurrence", schema = "emploidutemps")
public class SessionOccurrence extends TenantEntity {

    @Column(nullable = false)
    private UUID teachingAssignmentId;

    private UUID teacherId;

    private UUID subjectId;

    @Column(nullable = false)
    private UUID schoolClassId;

    private UUID roomId;

    @Column(nullable = false)
    private LocalDate sessionDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SessionType sessionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private OccurrenceStatus status;

    private boolean makeUp;

    private UUID makeUpRequestId;

    @Column(length = 500)
    private String cancellationReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id")
    private TimetableSlot slot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "make_up_of_id")
    private SessionOccurrence makeUpOf;

    public SessionOccurrence() {
    }

    public UUID getTeachingAssignmentId() {
        return teachingAssignmentId;
    }

    public void setTeachingAssignmentId(UUID teachingAssignmentId) {
        this.teachingAssignmentId = teachingAssignmentId;
    }

    public UUID getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(UUID teacherId) {
        this.teacherId = teacherId;
    }

    public UUID getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(UUID subjectId) {
        this.subjectId = subjectId;
    }

    public UUID getSchoolClassId() {
        return schoolClassId;
    }

    public void setSchoolClassId(UUID schoolClassId) {
        this.schoolClassId = schoolClassId;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public OccurrenceStatus getStatus() {
        return status;
    }

    public void setStatus(OccurrenceStatus status) {
        this.status = status;
    }

    public boolean isMakeUp() {
        return makeUp;
    }

    public void setMakeUp(boolean makeUp) {
        this.makeUp = makeUp;
    }

    public UUID getMakeUpRequestId() {
        return makeUpRequestId;
    }

    public void setMakeUpRequestId(UUID makeUpRequestId) {
        this.makeUpRequestId = makeUpRequestId;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public TimetableSlot getSlot() {
        return slot;
    }

    public void setSlot(TimetableSlot slot) {
        this.slot = slot;
    }

    public SessionOccurrence getMakeUpOf() {
        return makeUpOf;
    }

    public void setMakeUpOf(SessionOccurrence makeUpOf) {
        this.makeUpOf = makeUpOf;
    }

}
