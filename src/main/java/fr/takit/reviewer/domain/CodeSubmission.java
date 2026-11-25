package fr.takit.reviewer.domain;

/**
 * Java code submitted for review. Holds the size rules, so that every entry point
 * (web page, REST API, MCP tool) applies the same limits.
 */
public record CodeSubmission(String code) {

    public static final int MAX_LINES = 200;
    public static final int MAX_CHARS = 12_000;

    public CodeSubmission {
        if (code == null || code.isBlank()) {
            throw new InvalidSubmissionException("Paste some Java code to review.");
        }
        if (code.length() > MAX_CHARS) {
            throw new InvalidSubmissionException("The code is too long: " + MAX_CHARS + " characters at most.");
        }
        if (lineCount(code) > MAX_LINES) {
            throw new InvalidSubmissionException("The code is too long: " + MAX_LINES + " lines at most.");
        }
    }

    public int lines() {
        return lineCount(code);
    }

    /** The code with line numbers, so the model can point at a line. */
    public String numbered() {
        String[] rows = code.split("\\R", -1);
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < rows.length; i++) {
            out.append(String.format("%3d | %s%n", i + 1, rows[i]));
        }
        return out.toString();
    }

    private static int lineCount(String code) {
        return code.split("\\R", -1).length;
    }
}
