package app.model;

public class LogPattern {

    private final String name;
    private final String pattern;

    public LogPattern(String name, String pattern) {
        this.name = name;
        this.pattern = pattern;
    }

    public String getName() {
        return this.name;
    }

    public String getPattern() {
        return this.pattern;
    }

    @Override
    public String toString() {
        return name + ": {" + pattern + "}";
    }
}
