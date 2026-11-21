package tn.tekup.educated.administration.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tn.tekup.educated.shared.domain.BaseEntity;
import tn.tekup.educated.shared.domain.PersonType;

/** An application account. Passwords and 2FA secrets live in Keycloak. */
@Entity
@Table(name = "user_account", schema = "administration")
public class UserAccount extends BaseEntity {

    @Column(unique = true)
    private UUID keycloakId;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    private String email;

    @Column(length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private PersonType personType;

    private UUID personId;

    private boolean active;

    private boolean forcePasswordChange;

    private boolean twoFactorRequired;

    private Instant lastLoginAt;

    @Column(length = 5)
    private String preferredLocale;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ThemeMode theme;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private DisplayDensity displayDensity;

    @OneToMany(mappedBy = "userAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRole> roles = new ArrayList<>();

    @OneToMany(mappedBy = "userAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex")
    private List<UserFavorite> favorites = new ArrayList<>();

    public UserAccount() {
    }

    public UUID getKeycloakId() {
        return keycloakId;
    }

    public void setKeycloakId(UUID keycloakId) {
        this.keycloakId = keycloakId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public PersonType getPersonType() {
        return personType;
    }

    public void setPersonType(PersonType personType) {
        this.personType = personType;
    }

    public UUID getPersonId() {
        return personId;
    }

    public void setPersonId(UUID personId) {
        this.personId = personId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isForcePasswordChange() {
        return forcePasswordChange;
    }

    public void setForcePasswordChange(boolean forcePasswordChange) {
        this.forcePasswordChange = forcePasswordChange;
    }

    public boolean isTwoFactorRequired() {
        return twoFactorRequired;
    }

    public void setTwoFactorRequired(boolean twoFactorRequired) {
        this.twoFactorRequired = twoFactorRequired;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public String getPreferredLocale() {
        return preferredLocale;
    }

    public void setPreferredLocale(String preferredLocale) {
        this.preferredLocale = preferredLocale;
    }

    public ThemeMode getTheme() {
        return theme;
    }

    public void setTheme(ThemeMode theme) {
        this.theme = theme;
    }

    public DisplayDensity getDisplayDensity() {
        return displayDensity;
    }

    public void setDisplayDensity(DisplayDensity displayDensity) {
        this.displayDensity = displayDensity;
    }

    public List<UserRole> getRoles() {
        return roles;
    }

    public List<UserFavorite> getFavorites() {
        return favorites;
    }

}
