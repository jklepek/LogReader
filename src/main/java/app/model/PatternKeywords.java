package app.model;

import java.util.Optional;

public enum PatternKeywords {

    LEVEL("p"),
    THREAD("t"),
    MESSAGE("m"),
    EMITTER("c"),
    MDC("X"),
    TIMESTAMP("d"),
    LINE("L"),
    LOCATION("l"),
    PID("pid");

    private String label;

    PatternKeywords(String label) {
        this.label = label;
    }

    public static Optional<String> getKeyword(String definition) {
        for (PatternKeywords keyword : values()) {
            if (keyword.label.equals(definition)) {
                return Optional.of(keyword.name());
            }
        }
        return Optional.empty();
    }
}
