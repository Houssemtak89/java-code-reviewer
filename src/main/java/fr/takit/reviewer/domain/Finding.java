package fr.takit.reviewer.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Locale;

/**
 * One remark on the reviewed code.
 *
 * @param severity   how much it matters
 * @param category   what kind of remark it is
 * @param line       line number in the submitted code, or 0 when it applies to the whole snippet
 * @param message    the problem, in one or two sentences
 * @param suggestion how to fix it, possibly with a short code example
 */
public record Finding(Severity severity, Category category, int line, String message, String suggestion) {

    public Finding {
        severity = severity == null ? Severity.MINOR : severity;
        category = category == null ? Category.READABILITY : category;
        line = Math.max(line, 0);
        message = message == null ? "" : message.strip();
        suggestion = suggestion == null ? "" : suggestion.strip();
    }

    public enum Severity {
        BLOCKER, MAJOR, MINOR, INFO;

        /** Models do not always use our words: "critical", "high", "Medium"… are mapped, not rejected. */
        @JsonCreator
        public static Severity from(String value) {
            String v = value == null ? "" : value.strip().toUpperCase(Locale.ROOT);
            return switch (v) {
                case "BLOCKER", "CRITICAL", "SEVERE", "ERROR" -> BLOCKER;
                case "MAJOR", "HIGH", "WARNING" -> MAJOR;
                case "INFO", "LOW", "NOTE", "HINT" -> INFO;
                default -> MINOR;
            };
        }
    }

    public enum Category {
        BUG, CONCURRENCY, SECURITY, PERFORMANCE, READABILITY, DESIGN, TESTING;

        /** Exact names first, then a keyword guess ("NullPointerException" is a bug), readability last. */
        @JsonCreator
        public static Category from(String value) {
            String v = value == null ? "" : value.strip().toUpperCase(Locale.ROOT);
            for (Category c : values()) {
                if (c.name().equals(v)) {
                    return c;
                }
            }
            if (v.contains("THREAD") || v.contains("CONCURREN") || v.contains("RACE")) return CONCURRENCY;
            if (v.contains("SECUR") || v.contains("INJECT") || v.contains("VULN")) return SECURITY;
            if (v.contains("PERF")) return PERFORMANCE;
            if (v.contains("TEST")) return TESTING;
            if (v.contains("DESIGN") || v.contains("ARCHI")) return DESIGN;
            if (v.contains("BUG") || v.contains("EXCEPTION") || v.contains("NULL") || v.contains("LOGIC")
                    || v.contains("ERROR") || v.contains("ARITHMETIC") || v.contains("CORRECT")) return BUG;
            return READABILITY;
        }
    }
}
