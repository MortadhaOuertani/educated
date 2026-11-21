package tn.tekup.educated.administration.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.tekup.educated.shared.domain.PersonType;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    /** Roles of a user in the current establishment (the tenant filter applies). */
    @EntityGraph(attributePaths = "role")
    List<UserRole> findAllByUserAccount_Id(UUID userAccountId);

    boolean existsByUserAccount_IdAndRole_Id(UUID userAccountId, UUID roleId);

    /** Accounts of the given kind that have a role in the current establishment. */
    @Query("select distinct ur.userAccount.id from UserRole ur where ur.userAccount.personType = :type")
    List<UUID> findUserIdsByPersonType(@Param("type") PersonType type);

    /** Staff accounts of the current establishment, including accounts not linked to a person (administrators). */
    @Query("""
            select distinct ur.userAccount.id from UserRole ur
            where ur.userAccount.personType = tn.tekup.educated.shared.domain.PersonType.STAFF
               or ur.userAccount.personType is null
            """)
    List<UUID> findStaffUserIds();
}
