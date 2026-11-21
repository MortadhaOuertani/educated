package tn.tekup.educated.parametrage.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import tn.tekup.educated.shared.domain.Address;
import tn.tekup.educated.shared.domain.BaseEntity;
import tn.tekup.educated.shared.domain.LocalizedText;

/** A school or university using the platform (the tenant). */
@Entity
@Table(name = "establishment", schema = "parametrage")
public class Establishment extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Embedded
    private LocalizedText name;

    private String legalName;

    @Column(length = 40)
    private String taxNumber;

    @Embedded
    private Address address;

    private String email;

    @Column(length = 30)
    private String phone;

    private String website;

    private UUID logoFileId;

    @Column(nullable = false, length = 40)
    private String timezone;

    @Column(nullable = false, length = 5)
    private String defaultLocale;

    private boolean active;

    public Establishment() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalizedText getName() {
        return name;
    }

    public void setName(LocalizedText name) {
        this.name = name;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getTaxNumber() {
        return taxNumber;
    }

    public void setTaxNumber(String taxNumber) {
        this.taxNumber = taxNumber;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public UUID getLogoFileId() {
        return logoFileId;
    }

    public void setLogoFileId(UUID logoFileId) {
        this.logoFileId = logoFileId;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
