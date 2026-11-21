package tn.tekup.educated.pedagogie.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import tn.tekup.educated.shared.domain.TenantEntity;

@Entity
@Table(name = "program_chapter", schema = "pedagogie")
public class ProgramChapter extends TenantEntity {

    @Column(nullable = false)
    private Integer orderIndex;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String content;

    private Integer plannedHours;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private TeachingProgram program;

    public ProgramChapter() {
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getPlannedHours() {
        return plannedHours;
    }

    public void setPlannedHours(Integer plannedHours) {
        this.plannedHours = plannedHours;
    }

    public TeachingProgram getProgram() {
        return program;
    }

    public void setProgram(TeachingProgram program) {
        this.program = program;
    }

}
