package tn.tekup.educated.administration.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    /** Roles of a user in the current establishment (the tenant filter applies). */
    @EntityGraph(attributePaths = "role")
    List<UserRole> findAllByUserAccount_Id(UUID userAccountId);

    boolean existsByUserAccount_IdAndRole_Id(UUID userAccountId, UUID roleId);
}
