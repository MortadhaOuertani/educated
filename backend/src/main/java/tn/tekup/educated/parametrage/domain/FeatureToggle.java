package tn.tekup.educated.parametrage.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Turns a portal section on or off (e.g. the student Paiements section). */
@Entity
@Table(name = "feature_toggle", schema = "parametrage")
public class FeatureToggle extends TenantEntity {

    @Column(nullable = false, length = 80)
    private String featureKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Portal portal;

    private boolean enabled;

    @Column(length = 500)
    private String disabledMessage;

    public FeatureToggle() {
    }

    public String getFeatureKey() {
        return featureKey;
    }

    public void setFeatureKey(String featureKey) {
        this.featureKey = featureKey;
    }

    public Portal getPortal() {
        return portal;
    }

    public void setPortal(Portal portal) {
        this.portal = portal;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDisabledMessage() {
        return disabledMessage;
    }

    public void setDisabledMessage(String disabledMessage) {
        this.disabledMessage = disabledMessage;
    }

}
