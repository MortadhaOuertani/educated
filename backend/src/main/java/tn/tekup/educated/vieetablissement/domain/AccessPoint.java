package tn.tekup.educated.vieetablissement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import tn.tekup.educated.shared.domain.TenantEntity;

@Entity
@Table(name = "access_point", schema = "vieetablissement")
public class AccessPoint extends TenantEntity {

    private UUID siteId;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false)
    private String name;

    public AccessPoint() {
    }

    public UUID getSiteId() {
        return siteId;
    }

    public void setSiteId(UUID siteId) {
        this.siteId = siteId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
