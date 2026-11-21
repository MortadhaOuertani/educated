package tn.tekup.educated.administration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import tn.tekup.educated.shared.domain.TenantEntity;

/** A menu screen pinned by the user. */
@Entity
@Table(name = "user_favorite", schema = "administration")
public class UserFavorite extends TenantEntity {

    @Column(nullable = false, length = 200)
    private String navigationItem;

    @Column(nullable = false)
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_account_id", nullable = false)
    private UserAccount userAccount;

    public UserFavorite() {
    }

    public String getNavigationItem() {
        return navigationItem;
    }

    public void setNavigationItem(String navigationItem) {
        this.navigationItem = navigationItem;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

}
