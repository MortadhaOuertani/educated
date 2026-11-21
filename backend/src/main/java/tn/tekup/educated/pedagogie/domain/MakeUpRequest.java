package tn.tekup.educated.pedagogie.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Séance de rattrapage requested by a teacher. */
@Entity
@Table(name = "make_up_request", schema = "pedagogie")
public class MakeUpRequest extends TenantEntity {

    @Column(nullable = false)
    private UUID originalOccurrenceId;

    private UUID makeUpOccurrenceId;

    @Column(nullable = false)
    private LocalDate requestedDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    private UUID roomId;

    @Column(length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private MakeUpStatus status;

    private UUID decidedByUserId;

    private Instant decidedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teaching_assignment_id", nullable = false)
    private TeachingAssignment teachingAssignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    public MakeUpRequest() {
    }

    public UUID getOriginalOccurrenceId() {
        return originalOccurrenceId;
    }

    public void setOriginalOccurrenceId(UUID originalOccurrenceId) {
        this.originalOccurrenceId = originalOccurrenceId;
    }

    public UUID getMakeUpOccurrenceId() {
        return makeUpOccurrenceId;
    }

    public void setMakeUpOccurrenceId(UUID makeUpOccurrenceId) {
        this.makeUpOccurrenceId = makeUpOccurrenceId;
    }

    public LocalDate getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(LocalDate requestedDate) {
        this.requestedDate = requestedDate;
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

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public MakeUpStatus getStatus() {
        return status;
    }

    public void setStatus(MakeUpStatus status) {
        this.status = status;
    }

    public UUID getDecidedByUserId() {
        return decidedByUserId;
    }

    public void setDecidedByUserId(UUID decidedByUserId) {
        this.decidedByUserId = decidedByUserId;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(Instant decidedAt) {
        this.decidedAt = decidedAt;
    }

    public TeachingAssignment getTeachingAssignment() {
        return teachingAssignment;
    }

    public void setTeachingAssignment(TeachingAssignment teachingAssignment) {
        this.teachingAssignment = teachingAssignment;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

}
