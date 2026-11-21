package tn.tekup.educated.pedagogie.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Rendez-vous / Prise RDV request from a parent, student or staff member. */
@Entity
@Table(name = "meeting_request", schema = "pedagogie")
public class MeetingRequest extends TenantEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RequesterType requesterType;

    @Column(nullable = false)
    private UUID requesterPersonId;

    private UUID studentId;

    @Column(nullable = false)
    private LocalDate meetingDate;

    @Column(length = 500)
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private MeetingStatus status;

    @Column(length = 1000)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false)
    private TeacherMeetingSlot slot;

    public MeetingRequest() {
    }

    public RequesterType getRequesterType() {
        return requesterType;
    }

    public void setRequesterType(RequesterType requesterType) {
        this.requesterType = requesterType;
    }

    public UUID getRequesterPersonId() {
        return requesterPersonId;
    }

    public void setRequesterPersonId(UUID requesterPersonId) {
        this.requesterPersonId = requesterPersonId;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public LocalDate getMeetingDate() {
        return meetingDate;
    }

    public void setMeetingDate(LocalDate meetingDate) {
        this.meetingDate = meetingDate;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public MeetingStatus getStatus() {
        return status;
    }

    public void setStatus(MeetingStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public TeacherMeetingSlot getSlot() {
        return slot;
    }

    public void setSlot(TeacherMeetingSlot slot) {
        this.slot = slot;
    }

}
