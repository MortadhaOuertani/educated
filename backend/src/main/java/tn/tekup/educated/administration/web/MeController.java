package tn.tekup.educated.administration.web;

import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.tekup.educated.administration.domain.UserAccount;
import tn.tekup.educated.administration.domain.UserAccountRepository;
import tn.tekup.educated.administration.security.MyAccess;
import tn.tekup.educated.administration.web.AdministrationDtos.MeDto;
import tn.tekup.educated.administration.web.AdministrationDtos.PreferencesRequest;
import tn.tekup.educated.shared.security.CurrentUser;
import tn.tekup.educated.shared.security.CurrentUserProvider;
import tn.tekup.educated.shared.tenant.TenantContext;
import tn.tekup.educated.shared.web.NotFoundException;

/** The signed-in user's profile, access summary and preferences. Works for every signed-in user. */
@RestController
@RequestMapping("/api/me")
class MeController {

    private final CurrentUserProvider currentUserProvider;
    private final UserAccountRepository accounts;
    private final MyAccess myAccess;

    MeController(CurrentUserProvider currentUserProvider, UserAccountRepository accounts, MyAccess myAccess) {
        this.currentUserProvider = currentUserProvider;
        this.accounts = accounts;
        this.myAccess = myAccess;
    }

    @GetMapping
    @Transactional(readOnly = true)
    MeDto me() {
        CurrentUser user = currentUserProvider.require();
        UserAccount account = account(user);
        MyAccess.Summary access = myAccess.of(user);
        return new MeDto(user.userId(), user.username(), user.email(), user.firstName(), user.lastName(),
                user.personType(), user.personId(), user.administrative(), account.getTheme(),
                account.getDisplayDensity(), account.getPreferredLocale(),
                TenantContext.currentEstablishment().orElse(null), access.visibleModules(), access.permissions(),
                access.deniedActions());
    }

    @PutMapping("/preferences")
    @Transactional
    void updatePreferences(@Valid @RequestBody PreferencesRequest request) {
        UserAccount account = account(currentUserProvider.require());
        account.setTheme(request.theme());
        account.setDisplayDensity(request.displayDensity());
        account.setPreferredLocale(request.preferredLocale());
    }

    private UserAccount account(CurrentUser user) {
        return accounts.findById(user.userId()).orElseThrow(() -> new NotFoundException("UserAccount", user.userId()));
    }
}
