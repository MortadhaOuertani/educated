package tn.tekup.educated.shared.security;

import tn.tekup.educated.shared.domain.AppModule;

/**
 * Server-side permission checks, used in {@code @PreAuthorize("@perm.can('Student', 'READ')")}.
 * Implemented by the administration module as the bean named {@code perm}; the rules follow the
 * 6-layer role model (administrative flag, policy, type permissions, navigation, denied actions).
 */
public interface Permissions {

    String BEAN_NAME = "perm";

    /** May the current user perform the operation on the given entity type (e.g. "Student")? */
    boolean can(String targetType, Operation operation);

    /** Same as above with the operation as text, as written in {@code @PreAuthorize} expressions. */
    boolean can(String targetType, String operation);

    /** May the current user open the given menu entry (e.g. "Scolarite/Sanctions")? */
    boolean canNavigate(String navigationItem);

    /** Is the given action button (e.g. "Payer") not in the role's denied actions? */
    boolean canRunAction(String actionKey);

    /** Is the module shown in the current user's menu? (Display only; data access is checked by can().) */
    boolean isModuleVisible(AppModule module);
}
