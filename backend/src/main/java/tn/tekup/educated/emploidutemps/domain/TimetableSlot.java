package tn.tekup.educated.emploidutemps.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** A weekly slot of a timetable. */
@Entity
@Table(name = "timetable_slot", schema = "emploidutemps")
public class TimetableSlot extends TenantEntity {

    @Column(nullable = false)
    private UUID teachingAssignmentId;

    private UUID teacherId;

    private UUID subjectId;

    private UUID roomId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private WeekPattern weekPattern;

    private boolean outOfControl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "timetable_id", nullable = false)
    private Timetable timetable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_template_id")
    private TimeSlotTemplate timeSlotTemplate;

    public TimetableSlot() {
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

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
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

    public WeekPattern getWeekPattern() {
        return weekPattern;
    }

    public void setWeekPattern(WeekPattern weekPattern) {
        this.weekPattern = weekPattern;
    }

    public boolean isOutOfControl() {
        return outOfControl;
    }

    public void setOutOfControl(boolean outOfControl) {
        this.outOfControl = outOfControl;
    }

    public Timetable getTimetable() {
        return timetable;
    }

    public void setTimetable(Timetable timetable) {
        this.timetable = timetable;
    }

    public TimeSlotTemplate getTimeSlotTemplate() {
        return timeSlotTemplate;
    }

    public void setTimeSlotTemplate(TimeSlotTemplate timeSlotTemplate) {
        this.timeSlotTemplate = timeSlotTemplate;
    }

}
