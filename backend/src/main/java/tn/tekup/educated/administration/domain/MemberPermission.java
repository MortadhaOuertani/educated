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

/** Field-level rule inside a type permission. */
@Entity
@Table(name = "member_permission", schema = "administration")
public class MemberPermission extends TenantEntity {

    @Column(nullable = false, columnDefinition = "text")
    private String members;

    @Column(columnDefinition = "text")
    private String criteria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PermissionState canRead;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PermissionState canWrite;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_permission_id", nullable = false)
    private TypePermission typePermission;

    public MemberPermission() {
    }

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        this.members = members;
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

    public TypePermission getTypePermission() {
        return typePermission;
    }

    public void setTypePermission(TypePermission typePermission) {
        this.typePermission = typePermission;
    }

}
