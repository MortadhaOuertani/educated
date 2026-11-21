package tn.tekup.educated.examens.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tn.tekup.educated.shared.domain.AssessmentType;
import tn.tekup.educated.shared.domain.TenantEntity;

/** An exam (épreuve). */
@Entity
@Table(name = "exam", schema = "examens")
public class Exam extends TenantEntity {

    @Column(nullable = false)
    private UUID studyPlanSubjectId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AssessmentType assessmentType;

    @Column(nullable = false)
    private LocalDate examDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    private Integer durationMinutes;

    private boolean published;

    private boolean roomsPublished;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_session_id", nullable = false)
    private ExamSession examSession;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExamClass> classes = new ArrayList<>();

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExamRoomAllocation> rooms = new ArrayList<>();

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Invigilation> invigilations = new ArrayList<>();

    public Exam() {
    }

    public UUID getStudyPlanSubjectId() {
        return studyPlanSubjectId;
    }

    public void setStudyPlanSubjectId(UUID studyPlanSubjectId) {
        this.studyPlanSubjectId = studyPlanSubjectId;
    }

    public AssessmentType getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(AssessmentType assessmentType) {
        this.assessmentType = assessmentType;
    }

    public LocalDate getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
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

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public boolean isRoomsPublished() {
        return roomsPublished;
    }

    public void setRoomsPublished(boolean roomsPublished) {
        this.roomsPublished = roomsPublished;
    }

    public ExamSession getExamSession() {
        return examSession;
    }

    public void setExamSession(ExamSession examSession) {
        this.examSession = examSession;
    }

    public List<ExamClass> getClasses() {
        return classes;
    }

    public List<ExamRoomAllocation> getRooms() {
        return rooms;
    }

    public List<Invigilation> getInvigilations() {
        return invigilations;
    }

}
