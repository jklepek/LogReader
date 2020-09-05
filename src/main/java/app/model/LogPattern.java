package app.model;

public class LogPattern {

    private String name;
    private String pattern;

    public LogPattern(String name, String pattern) {
        this.name = name;
        this.pattern = pattern;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPattern() {
        return this.pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String toString() {
        return name + ": {" + pattern + "}";
    }
}
