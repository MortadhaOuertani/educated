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

/** Row-level rule inside a type permission (criteria expression). */
@Entity
@Table(name = "object_permission", schema = "administration")
public class ObjectPermission extends TenantEntity {

    @Column(nullable = false, columnDefinition = "text")
    private String criteria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PermissionState canRead;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PermissionState canWrite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PermissionState canDelete;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PermissionState canNavigate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_permission_id", nullable = false)
    private TypePermission typePermission;

    public ObjectPermission() {
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
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

    public TypePermission getTypePermission() {
        return typePermission;
    }

    public void setTypePermission(TypePermission typePermission) {
        this.typePermission = typePermission;
    }

}
