package tn.tekup.educated.shared.domain;

import jakarta.persistence.Embeddable;

/** A label in the three working languages (French, English, Arabic). */
@Embeddable
public record LocalizedText(String fr, String en, String ar) {

    public static LocalizedText of(String fr) {
        return new LocalizedText(fr, null, null);
    }

    /** Returns the text in the requested language, falling back to French. */
    public String in(String language) {
        String value = switch (language == null ? "fr" : language) {
            case "en" -> en;
            case "ar" -> ar;
            default -> fr;
        };
        return value == null || value.isBlank() ? fr : value;
    }
}
