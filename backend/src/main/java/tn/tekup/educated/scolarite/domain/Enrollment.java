package tn.tekup.educated.scolarite.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Inscription of a learner in a study level for one academic year. */
@Entity
@Table(name = "enrollment", schema = "scolarite")
public class Enrollment extends TenantEntity {

    @Column(nullable = false)
    private UUID academicYearId;

    @Column(nullable = false)
    private UUID studyLevelId;

    @Column(nullable = false)
    private LocalDate enrollmentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EnrollmentType enrollmentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EnrollmentStatus status;

    private boolean repeating;

    private Instant registrationFormPrintedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startDate")
    private List<ClassAssignment> assignments = new ArrayList<>();

    public Enrollment() {
    }

    public UUID getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(UUID academicYearId) {
        this.academicYearId = academicYearId;
    }

    public UUID getStudyLevelId() {
        return studyLevelId;
    }

    public void setStudyLevelId(UUID studyLevelId) {
        this.studyLevelId = studyLevelId;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public EnrollmentType getEnrollmentType() {
        return enrollmentType;
    }

    public void setEnrollmentType(EnrollmentType enrollmentType) {
        this.enrollmentType = enrollmentType;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public boolean isRepeating() {
        return repeating;
    }

    public void setRepeating(boolean repeating) {
        this.repeating = repeating;
    }

    public Instant getRegistrationFormPrintedAt() {
        return registrationFormPrintedAt;
    }

    public void setRegistrationFormPrintedAt(Instant registrationFormPrintedAt) {
        this.registrationFormPrintedAt = registrationFormPrintedAt;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public List<ClassAssignment> getAssignments() {
        return assignments;
    }

}
