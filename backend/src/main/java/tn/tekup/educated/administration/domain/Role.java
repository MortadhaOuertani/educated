package tn.tekup.educated.administration.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import tn.tekup.educated.shared.domain.TenantEntity;

/** A back-office profile built by the Super Admin (6-layer role model). */
@Entity
@Table(name = "role", schema = "administration")
public class Role extends TenantEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    private boolean administrative;

    private boolean canEditModel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PermissionPolicy permissionPolicy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RolePreset preset;

    private boolean systemRole;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoleModuleAccess> moduleAccess = new ArrayList<>();

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("targetType")
    private List<TypePermission> typePermissions = new ArrayList<>();

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("itemPath")
    private List<NavigationPermission> navigationPermissions = new ArrayList<>();

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("actionKey")
    private List<DeniedAction> deniedActions = new ArrayList<>();

    public Role() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAdministrative() {
        return administrative;
    }

    public void setAdministrative(boolean administrative) {
        this.administrative = administrative;
    }

    public boolean isCanEditModel() {
        return canEditModel;
    }

    public void setCanEditModel(boolean canEditModel) {
        this.canEditModel = canEditModel;
    }

    public PermissionPolicy getPermissionPolicy() {
        return permissionPolicy;
    }

    public void setPermissionPolicy(PermissionPolicy permissionPolicy) {
        this.permissionPolicy = permissionPolicy;
    }

    public RolePreset getPreset() {
        return preset;
    }

    public void setPreset(RolePreset preset) {
        this.preset = preset;
    }

    public boolean isSystemRole() {
        return systemRole;
    }

    public void setSystemRole(boolean systemRole) {
        this.systemRole = systemRole;
    }

    public List<RoleModuleAccess> getModuleAccess() {
        return moduleAccess;
    }

    public List<TypePermission> getTypePermissions() {
        return typePermissions;
    }

    public List<NavigationPermission> getNavigationPermissions() {
        return navigationPermissions;
    }

    public List<DeniedAction> getDeniedActions() {
        return deniedActions;
    }

}
