package tn.tekup.educated.parametrage.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import tn.tekup.educated.shared.domain.LocalizedText;
import tn.tekup.educated.shared.domain.TenantEntity;

/** An entry of the academic calendar (holiday, exam period, make-up week...). */
@Entity
@Table(name = "calendar_event", schema = "parametrage")
public class CalendarEvent extends TenantEntity {

    @Embedded
    private LocalizedText title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CalendarEventType eventType;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private boolean blocksClasses;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @ManyToMany
    @JoinTable(name = "calendar_event_level", schema = "parametrage",
            joinColumns = @JoinColumn(name = "calendar_event_id"),
            inverseJoinColumns = @JoinColumn(name = "study_level_id"))
    private Set<StudyLevel> appliesTo = new HashSet<>();

    public CalendarEvent() {
    }

    public LocalizedText getTitle() {
        return title;
    }

    public void setTitle(LocalizedText title) {
        this.title = title;
    }

    public CalendarEventType getEventType() {
        return eventType;
    }

    public void setEventType(CalendarEventType eventType) {
        this.eventType = eventType;
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

    public boolean isBlocksClasses() {
        return blocksClasses;
    }

    public void setBlocksClasses(boolean blocksClasses) {
        this.blocksClasses = blocksClasses;
    }

    public AcademicYear getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(AcademicYear academicYear) {
        this.academicYear = academicYear;
    }

    public Set<StudyLevel> getAppliesTo() {
        return appliesTo;
    }

}
