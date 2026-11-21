package tn.tekup.educated.examens.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Decision of the class jury for a student and a year. */
@Entity
@Table(name = "class_decision", schema = "examens")
public class ClassDecision extends TenantEntity {

    @Column(nullable = false)
    private UUID studentId;

    @Column(nullable = false)
    private UUID academicYearId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private DecisionType decision;

    private LocalDate juryDate;

    @Column(length = 1000)
    private String comment;

    public ClassDecision() {
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

    public DecisionType getDecision() {
        return decision;
    }

    public void setDecision(DecisionType decision) {
        this.decision = decision;
    }

    public LocalDate getJuryDate() {
        return juryDate;
    }

    public void setJuryDate(LocalDate juryDate) {
        this.juryDate = juryDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
