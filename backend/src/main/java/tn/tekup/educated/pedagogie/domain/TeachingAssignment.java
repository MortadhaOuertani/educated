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
import tn.tekup.educated.shared.domain.SessionType;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Who teaches which subject to which class, and for how many hours. */
@Entity
@Table(name = "teaching_assignment", schema = "pedagogie")
public class TeachingAssignment extends TenantEntity {

    @Column(nullable = false)
    private UUID schoolClassId;

    @Column(nullable = false)
    private UUID academicYearId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SessionType sessionType;

    private Integer plannedHours;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_plan_subject_id", nullable = false)
    private StudyPlanSubject studyPlanSubject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workload_id")
    private TeacherWorkload workload;

    public TeachingAssignment() {
    }

    public UUID getSchoolClassId() {
        return schoolClassId;
    }

    public void setSchoolClassId(UUID schoolClassId) {
        this.schoolClassId = schoolClassId;
    }

    public UUID getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(UUID academicYearId) {
        this.academicYearId = academicYearId;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public Integer getPlannedHours() {
        return plannedHours;
    }

    public void setPlannedHours(Integer plannedHours) {
        this.plannedHours = plannedHours;
    }

    public StudyPlanSubject getStudyPlanSubject() {
        return studyPlanSubject;
    }

    public void setStudyPlanSubject(StudyPlanSubject studyPlanSubject) {
        this.studyPlanSubject = studyPlanSubject;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public TeacherWorkload getWorkload() {
        return workload;
    }

    public void setWorkload(TeacherWorkload workload) {
        this.workload = workload;
    }

}
