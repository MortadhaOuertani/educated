package tn.tekup.educated.examens.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import tn.tekup.educated.shared.domain.LocalizedText;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Appreciation by average range (Très bien, Bien...). */
@Entity
@Table(name = "appreciation_scale", schema = "examens")
public class AppreciationScale extends TenantEntity {

    @Embedded
    private LocalizedText label;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal minAverage;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal maxAverage;

    public AppreciationScale() {
    }

    public LocalizedText getLabel() {
        return label;
    }

    public void setLabel(LocalizedText label) {
        this.label = label;
    }

    public BigDecimal getMinAverage() {
        return minAverage;
    }

    public void setMinAverage(BigDecimal minAverage) {
        this.minAverage = minAverage;
    }

    public BigDecimal getMaxAverage() {
        return maxAverage;
    }

    public void setMaxAverage(BigDecimal maxAverage) {
        this.maxAverage = maxAverage;
    }

}
