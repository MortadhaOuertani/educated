package tn.tekup.educated.assiduite.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Thresholds and deadlines for calls (10 % at risk, 25 % critical...). */
@Entity
@Table(name = "attendance_policy", schema = "assiduite")
public class AttendancePolicy extends TenantEntity {

    private UUID studyLevelId;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal atRiskPercent;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal criticalPercent;

    @Column(nullable = false)
    private Integer callDeadlineMinutes;

    @Column(nullable = false)
    private Integer editWindowHours;

    private boolean countOnlyConfirmedCalls;

    public AttendancePolicy() {
    }

    public UUID getStudyLevelId() {
        return studyLevelId;
    }

    public void setStudyLevelId(UUID studyLevelId) {
        this.studyLevelId = studyLevelId;
    }

    public BigDecimal getAtRiskPercent() {
        return atRiskPercent;
    }

    public void setAtRiskPercent(BigDecimal atRiskPercent) {
        this.atRiskPercent = atRiskPercent;
    }

    public BigDecimal getCriticalPercent() {
        return criticalPercent;
    }

    public void setCriticalPercent(BigDecimal criticalPercent) {
        this.criticalPercent = criticalPercent;
    }

    public Integer getCallDeadlineMinutes() {
        return callDeadlineMinutes;
    }

    public void setCallDeadlineMinutes(Integer callDeadlineMinutes) {
        this.callDeadlineMinutes = callDeadlineMinutes;
    }

    public Integer getEditWindowHours() {
        return editWindowHours;
    }

    public void setEditWindowHours(Integer editWindowHours) {
        this.editWindowHours = editWindowHours;
    }

    public boolean isCountOnlyConfirmedCalls() {
        return countOnlyConfirmedCalls;
    }

    public void setCountOnlyConfirmedCalls(boolean countOnlyConfirmedCalls) {
        this.countOnlyConfirmedCalls = countOnlyConfirmedCalls;
    }

}
