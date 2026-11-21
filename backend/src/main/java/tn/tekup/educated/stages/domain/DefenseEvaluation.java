package tn.tekup.educated.stages.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import tn.tekup.educated.shared.domain.TenantEntity;

@Entity
@Table(name = "defense_evaluation", schema = "stages")
public class DefenseEvaluation extends TenantEntity {

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @Column(length = 1000)
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "defense_id", nullable = false)
    private Defense defense;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jury_member_id", nullable = false)
    private JuryMember juryMember;

    public DefenseEvaluation() {
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Defense getDefense() {
        return defense;
    }

    public void setDefense(Defense defense) {
        this.defense = defense;
    }

    public JuryMember getJuryMember() {
        return juryMember;
    }

    public void setJuryMember(JuryMember juryMember) {
        this.juryMember = juryMember;
    }

}
