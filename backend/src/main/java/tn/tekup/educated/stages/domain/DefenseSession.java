package tn.tekup.educated.stages.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Session de soutenance. */
@Entity
@Table(name = "defense_session", schema = "stages")
public class DefenseSession extends TenantEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private UUID academicYearId;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private LocalDate requestsOpenFrom;

    private LocalDate requestsOpenTo;

    @OneToMany(mappedBy = "defenseSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("juryDate")
    private List<Jury> juries = new ArrayList<>();

    public DefenseSession() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(UUID academicYearId) {
        this.academicYearId = academicYearId;
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

    public LocalDate getRequestsOpenFrom() {
        return requestsOpenFrom;
    }

    public void setRequestsOpenFrom(LocalDate requestsOpenFrom) {
        this.requestsOpenFrom = requestsOpenFrom;
    }

    public LocalDate getRequestsOpenTo() {
        return requestsOpenTo;
    }

    public void setRequestsOpenTo(LocalDate requestsOpenTo) {
        this.requestsOpenTo = requestsOpenTo;
    }

    public List<Jury> getJuries() {
        return juries;
    }

}
