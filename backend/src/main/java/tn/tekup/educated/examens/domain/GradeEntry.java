package tn.tekup.educated.examens.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** A grade, or the reason there is none (absence, exemption, fraud). */
@Entity
@Table(name = "grade_entry", schema = "examens")
public class GradeEntry extends TenantEntity {

    @Column(nullable = false)
    private UUID studentId;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private GradeReason reason;

    @Column(length = 500)
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grid_id", nullable = false)
    private GradeGrid grid;

    public GradeEntry() {
    }

    public UUID getStudentId() {
        return studentId;
    }

    public void setStudentId(UUID studentId) {
        this.studentId = studentId;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public GradeReason getReason() {
        return reason;
    }

    public void setReason(GradeReason reason) {
        this.reason = reason;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public GradeGrid getGrid() {
        return grid;
    }

    public void setGrid(GradeGrid grid) {
        this.grid = grid;
    }

}
