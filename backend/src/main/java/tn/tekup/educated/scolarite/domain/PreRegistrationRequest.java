package tn.tekup.educated.scolarite.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tn.tekup.educated.shared.domain.PersonInfo;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Demande de préinscription. */
@Entity
@Table(name = "pre_registration_request", schema = "scolarite")
public class PreRegistrationRequest extends TenantEntity {

    @Embedded
    private PersonInfo applicant;

    @Column(nullable = false)
    private UUID programId;

    @Column(nullable = false)
    private UUID studyLevelId;

    @Column(nullable = false)
    private UUID academicYearId;

    @Column(length = 100)
    private String baccalaureateSection;

    @Column(precision = 5, scale = 2)
    private BigDecimal baccalaureateAverage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PreRegistrationStatus status;

    private UUID reviewedByUserId;

    private Instant reviewedAt;

    @Column(length = 1000)
    private String decisionNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "converted_to_id")
    private Student convertedTo;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PreRegistrationDocument> documents = new ArrayList<>();

    public PreRegistrationRequest() {
    }

    public PersonInfo getApplicant() {
        return applicant;
    }

    public void setApplicant(PersonInfo applicant) {
        this.applicant = applicant;
    }

    public UUID getProgramId() {
        return programId;
    }

    public void setProgramId(UUID programId) {
        this.programId = programId;
    }

    public UUID getStudyLevelId() {
        return studyLevelId;
    }

    public void setStudyLevelId(UUID studyLevelId) {
        this.studyLevelId = studyLevelId;
    }

    public UUID getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(UUID academicYearId) {
        this.academicYearId = academicYearId;
    }

    public String getBaccalaureateSection() {
        return baccalaureateSection;
    }

    public void setBaccalaureateSection(String baccalaureateSection) {
        this.baccalaureateSection = baccalaureateSection;
    }

    public BigDecimal getBaccalaureateAverage() {
        return baccalaureateAverage;
    }

    public void setBaccalaureateAverage(BigDecimal baccalaureateAverage) {
        this.baccalaureateAverage = baccalaureateAverage;
    }

    public PreRegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(PreRegistrationStatus status) {
        this.status = status;
    }

    public UUID getReviewedByUserId() {
        return reviewedByUserId;
    }

    public void setReviewedByUserId(UUID reviewedByUserId) {
        this.reviewedByUserId = reviewedByUserId;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getDecisionNote() {
        return decisionNote;
    }

    public void setDecisionNote(String decisionNote) {
        this.decisionNote = decisionNote;
    }

    public Student getConvertedTo() {
        return convertedTo;
    }

    public void setConvertedTo(Student convertedTo) {
        this.convertedTo = convertedTo;
    }

    public List<PreRegistrationDocument> getDocuments() {
        return documents;
    }

}
