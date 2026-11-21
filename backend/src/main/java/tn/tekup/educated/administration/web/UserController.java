package tn.tekup.educated.administration.web;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.tekup.educated.administration.domain.RoleChangeRequest;
import tn.tekup.educated.administration.service.RoleAdministration;
import tn.tekup.educated.administration.service.RoleAssignments;
import tn.tekup.educated.administration.web.AdministrationDtos.ActiveRequest;
import tn.tekup.educated.administration.web.AdministrationDtos.AssignRoleRequest;
import tn.tekup.educated.administration.web.AdministrationDtos.ChangeRequestDto;
import tn.tekup.educated.administration.web.AdministrationDtos.RoleSummaryDto;
import tn.tekup.educated.administration.web.AdministrationDtos.UserDetailDto;
import tn.tekup.educated.administration.web.AdministrationDtos.UserDto;
import tn.tekup.educated.shared.web.PageResponse;

@RestController
@RequestMapping("/api/administration/users")
class UserController {

    private final RoleAdministration administration;
    private final RoleAssignments roleAssignments;

    UserController(RoleAdministration administration, RoleAssignments roleAssignments) {
        this.administration = administration;
        this.roleAssignments = roleAssignments;
    }

    @GetMapping
    @PreAuthorize("@perm.can('UserAccount', 'READ')")
    PageResponse<UserDto> users(@RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("username"));
        return PageResponse.of(administration.users(search, pageable), UserDto::from);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.can('UserAccount', 'READ')")
    @Transactional(readOnly = true)
    UserDetailDto user(@PathVariable UUID id) {
        return new UserDetailDto(UserDto.from(administration.user(id)),
                roleAssignments.activeRoles(id).stream().map(RoleSummaryDto::from).toList());
    }

    /** 200 when the role is given; 202 with a pending request when it is an admin role needing approval. */
    @PostMapping("/{id}/roles")
    @PreAuthorize("@perm.can('UserAccount', 'WRITE')")
    @Transactional
    ResponseEntity<ChangeRequestDto> assignRole(@PathVariable UUID id, @Valid @RequestBody AssignRoleRequest request) {
        RoleChangeRequest pending = administration.assignRole(id, request.roleId());
        if (pending == null) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ChangeRequestDto.from(pending));
    }

    @DeleteMapping("/{id}/roles/{roleId}")
    @PreAuthorize("@perm.can('UserAccount', 'WRITE')")
    void removeRole(@PathVariable UUID id, @PathVariable UUID roleId) {
        administration.removeRole(id, roleId);
    }

    @PutMapping("/{id}/active")
    @PreAuthorize("@perm.can('UserAccount', 'WRITE')")
    UserDto setActive(@PathVariable UUID id, @RequestBody ActiveRequest request) {
        return UserDto.from(administration.setActive(id, request.active()));
    }
}
