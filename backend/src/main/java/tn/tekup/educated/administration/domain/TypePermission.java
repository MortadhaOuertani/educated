package tn.tekup.educated.administration.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import tn.tekup.educated.shared.domain.TenantEntity;

/** Layer 4: read/write/create/delete/navigate rights on one entity type. */
@Entity
@Table(name = "type_permission", schema = "administration")
public class TypePermission extends TenantEntity {

    @Column(nullable = false, length = 100)
    private String targetType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PermissionState canRead;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PermissionState canWrite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PermissionState canCreate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PermissionState canDelete;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PermissionState canNavigate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @OneToMany(mappedBy = "typePermission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ObjectPermission> objectPermissions = new ArrayList<>();

    @OneToMany(mappedBy = "typePermission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberPermission> memberPermissions = new ArrayList<>();

    public TypePermission() {
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public PermissionState getCanRead() {
        return canRead;
    }

    public void setCanRead(PermissionState canRead) {
        this.canRead = canRead;
    }

    public PermissionState getCanWrite() {
        return canWrite;
    }

    public void setCanWrite(PermissionState canWrite) {
        this.canWrite = canWrite;
    }

    public PermissionState getCanCreate() {
        return canCreate;
    }

    public void setCanCreate(PermissionState canCreate) {
        this.canCreate = canCreate;
    }

    public PermissionState getCanDelete() {
        return canDelete;
    }

    public void setCanDelete(PermissionState canDelete) {
        this.canDelete = canDelete;
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

    public List<ObjectPermission> getObjectPermissions() {
        return objectPermissions;
    }

    public List<MemberPermission> getMemberPermissions() {
        return memberPermissions;
    }

}
