package tn.tekup.educated.administration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Layer 5: access to one menu entry. */
@Entity
@Table(name = "navigation_permission", schema = "administration")
public class NavigationPermission extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String itemPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PermissionState canNavigate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    public NavigationPermission() {
    }

    public String getItemPath() {
        return itemPath;
    }

    public void setItemPath(String itemPath) {
        this.itemPath = itemPath;
    }

    public PermissionState getCanNavigate() {
        return canNavigate;
    }

    public void setCanNavigate(PermissionState canNavigate) {
        this.canNavigate = canNavigate;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

}
