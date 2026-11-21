package tn.tekup.educated.pedagogie.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Programme d'enseignement of a subject. */
@Entity
@Table(name = "teaching_program", schema = "pedagogie")
public class TeachingProgram extends TenantEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String objectives;

    @Column(columnDefinition = "text")
    private String prerequisites;

    @Column(columnDefinition = "text")
    private String bibliography;

    private Integer versionNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_plan_subject_id", nullable = false)
    private StudyPlanSubject studyPlanSubject;

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex")
    private List<ProgramChapter> chapters = new ArrayList<>();

    public TeachingProgram() {
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

    public String getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(String prerequisites) {
        this.prerequisites = prerequisites;
    }

    public String getBibliography() {
        return bibliography;
    }

    public void setBibliography(String bibliography) {
        this.bibliography = bibliography;
    }

    public Integer getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(Integer versionNumber) {
        this.versionNumber = versionNumber;
    }

    public StudyPlanSubject getStudyPlanSubject() {
        return studyPlanSubject;
    }

    public void setStudyPlanSubject(StudyPlanSubject studyPlanSubject) {
        this.studyPlanSubject = studyPlanSubject;
    }

    public List<ProgramChapter> getChapters() {
        return chapters;
    }

}
