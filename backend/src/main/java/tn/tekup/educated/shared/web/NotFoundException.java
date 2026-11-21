package tn.tekup.educated.shared.web;

/** Thrown when a requested record does not exist in the current establishment (HTTP 404). */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String entityType, Object id) {
        super(entityType + " not found: " + id);
    }
}
