package tn.tekup.educated.examens.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** An exam session (main, resit, continuous). */
@Entity
@Table(name = "exam_session", schema = "examens")
public class ExamSession extends TenantEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ExamSessionType sessionType;

    @Column(nullable = false)
    private UUID academicPeriodId;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private boolean published;

    @OneToMany(mappedBy = "examSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("examDate")
    private List<Exam> exams = new ArrayList<>();

    public ExamSession() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExamSessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(ExamSessionType sessionType) {
        this.sessionType = sessionType;
    }

    public UUID getAcademicPeriodId() {
        return academicPeriodId;
    }

    public void setAcademicPeriodId(UUID academicPeriodId) {
        this.academicPeriodId = academicPeriodId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public List<Exam> getExams() {
        return exams;
    }

}
