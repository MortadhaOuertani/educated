package tn.tekup.educated.stages.domain;

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

/** An internship (stage) and its approval timeline. */
@Entity
@Table(name = "internship", schema = "stages")
public class Internship extends TenantEntity {

    @Column(nullable = false)
    private UUID studentId;

    @Column(nullable = false)
    private UUID academicYearId;

    private UUID academicSupervisorId;

    @Column(nullable = false, length = 40)
    private String referenceNumber;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "text")
    private String objectives;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private InternshipStatus status;

    private UUID approvedByUserId;

    private Instant approvedAt;

    @Column(length = 1000)
    private String refusalReason;

    private Instant defenseRequestedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nature_id", nullable = false)
    private InternshipNature nature;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_supervisor_id")
    private CompanySupervisor companySupervisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_session_id")
    private DefenseSession requestedSession;

    @OneToMany(mappedBy = "internship", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt")
    private List<InternshipStatusChange> statusChanges = new ArrayList<>();

    @OneToMany(mappedBy = "internship", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InternshipDocument> documents = new ArrayList<>();

    public Internship() {
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public UUID getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(UUID academicYearId) {
        this.academicYearId = academicYearId;
    }

    public UUID getAcademicSupervisorId() {
        return academicSupervisorId;
    }

    public void setAcademicSupervisorId(UUID academicSupervisorId) {
        this.academicSupervisorId = academicSupervisorId;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getObjectives() {
        return objectives;
    }

    public void setObjectives(String objectives) {
        this.objectives = objectives;
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

    public InternshipStatus getStatus() {
        return status;
    }

    public void setStatus(InternshipStatus status) {
        this.status = status;
    }

    public UUID getApprovedByUserId() {
        return approvedByUserId;
    }

    public void setApprovedByUserId(UUID approvedByUserId) {
        this.approvedByUserId = approvedByUserId;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Instant approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getRefusalReason() {
        return refusalReason;
    }

    public void setRefusalReason(String refusalReason) {
        this.refusalReason = refusalReason;
    }

    public Instant getDefenseRequestedAt() {
        return defenseRequestedAt;
    }

    public void setDefenseRequestedAt(Instant defenseRequestedAt) {
        this.defenseRequestedAt = defenseRequestedAt;
    }

    public InternshipNature getNature() {
        return nature;
    }

    public void setNature(InternshipNature nature) {
        this.nature = nature;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public CompanySupervisor getCompanySupervisor() {
        return companySupervisor;
    }

    public void setCompanySupervisor(CompanySupervisor companySupervisor) {
        this.companySupervisor = companySupervisor;
    }

    public DefenseSession getRequestedSession() {
        return requestedSession;
    }

    public void setRequestedSession(DefenseSession requestedSession) {
        this.requestedSession = requestedSession;
    }

    public List<InternshipStatusChange> getStatusChanges() {
        return statusChanges;
    }

    public List<InternshipDocument> getDocuments() {
        return documents;
    }

}
