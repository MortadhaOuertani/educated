package tn.tekup.educated.parametrage.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tn.tekup.educated.shared.domain.Address;
import tn.tekup.educated.shared.domain.TenantEntity;

/** A campus or building group of the establishment. */
@Entity
@Table(name = "site", schema = "parametrage")
public class Site extends TenantEntity {

    @Column(nullable = false, length = 20)
    private String code;

    @Column(nullable = false)
    private String name;

    @Embedded
    private Address address;

    public Site() {
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

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

}
