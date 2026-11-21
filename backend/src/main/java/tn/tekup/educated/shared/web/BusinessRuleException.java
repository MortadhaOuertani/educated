package tn.tekup.educated.shared.web;

/**
 * A business rule was broken (HTTP 422), e.g. "a grade has either a score or a reason".
 * The code is stable so the front-ends can translate the message.
 */
public class BusinessRuleException extends RuntimeException {

    private final String code;

    public BusinessRuleException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
