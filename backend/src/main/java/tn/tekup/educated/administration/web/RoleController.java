package tn.tekup.educated.administration.web;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tn.tekup.educated.administration.service.RoleAdministration;
import tn.tekup.educated.administration.service.RoleAdministration.ChangePayload;
import tn.tekup.educated.administration.service.RoleAdministration.NavigationRule;
import tn.tekup.educated.administration.service.RoleAdministration.TypeRule;
import tn.tekup.educated.administration.web.AdministrationDtos.ChangeRequestBody;
import tn.tekup.educated.administration.web.AdministrationDtos.ChangeRequestDto;
import tn.tekup.educated.administration.web.AdministrationDtos.NewRoleRequest;
import tn.tekup.educated.administration.web.AdministrationDtos.RoleDetailDto;
import tn.tekup.educated.administration.web.AdministrationDtos.RoleLayersRequest;
import tn.tekup.educated.administration.web.AdministrationDtos.RoleSummaryDto;

/** The role editor: roles, their permission layers, and approval of sensitive changes. */
@RestController
@RequestMapping("/api/administration")
class RoleController {

    private final RoleAdministration administration;

    RoleController(RoleAdministration administration) {
        this.administration = administration;
    }

    @GetMapping("/roles")
    @PreAuthorize("@perm.can('Role', 'READ')")
    List<RoleSummaryDto> roles() {
        return administration.roles().stream().map(RoleSummaryDto::from).toList();
    }

    @GetMapping("/roles/{id}")
    @PreAuthorize("@perm.can('Role', 'READ')")
    @Transactional(readOnly = true)
    RoleDetailDto role(@PathVariable UUID id) {
        return RoleDetailDto.from(administration.role(id));
    }

    @PostMapping("/roles")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.can('Role', 'CREATE')")
    RoleSummaryDto createRole(@Valid @RequestBody NewRoleRequest request) {
        return RoleSummaryDto.from(
                administration.createRole(request.name(), request.description(), request.permissionPolicy()));
    }

    @PutMapping("/roles/{id}/layers")
    @PreAuthorize("@perm.can('Role', 'WRITE')")
    @Transactional
    RoleDetailDto updateLayers(@PathVariable UUID id, @Valid @RequestBody RoleLayersRequest request) {
        List<TypeRule> types = request.typePermissions().stream()
                .map(t -> new TypeRule(t.targetType(), t.read(), t.write(), t.create(), t.delete(), t.navigate()))
                .toList();
        List<NavigationRule> navigation = request.navigationPermissions().stream()
                .map(n -> new NavigationRule(n.itemPath(), n.navigate()))
                .toList();
        return RoleDetailDto.from(administration.updateLayers(id, request.description(), request.visibleModules(),
                types, navigation, request.deniedActions()));
    }

    /** Policy and admin-flag changes only create a request; another Super Admin must approve it. */
    @PostMapping("/roles/{id}/change-requests")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("@perm.can('Role', 'WRITE')")
    @Transactional
    ChangeRequestDto requestChange(@PathVariable UUID id, @Valid @RequestBody ChangeRequestBody body) {
        ChangePayload payload = new ChangePayload(null, body.permissionPolicy(), body.administrative(),
                body.canEditModel());
        return ChangeRequestDto.from(administration.requestChange(id, body.changeType(), payload));
    }

    @GetMapping("/role-change-requests")
    @PreAuthorize("@perm.can('RoleChangeRequest', 'READ')")
    @Transactional(readOnly = true)
    List<ChangeRequestDto> pendingRequests() {
        return administration.pendingRequests().stream().map(ChangeRequestDto::from).toList();
    }

    @PostMapping("/role-change-requests/{id}/approve")
    @PreAuthorize("@perm.can('RoleChangeRequest', 'WRITE')")
    @Transactional
    ChangeRequestDto approve(@PathVariable UUID id) {
        return ChangeRequestDto.from(administration.decide(id, true));
    }

    @PostMapping("/role-change-requests/{id}/reject")
    @PreAuthorize("@perm.can('RoleChangeRequest', 'WRITE')")
    @Transactional
    ChangeRequestDto reject(@PathVariable UUID id) {
        return ChangeRequestDto.from(administration.decide(id, false));
    }
}
