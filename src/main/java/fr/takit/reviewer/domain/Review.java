package fr.takit.reviewer.domain;

import java.util.Comparator;
import java.util.List;

/** The result of a review: a one-line summary and the findings, most severe first. */
public record Review(String summary, List<Finding> findings) {

    public Review {
        summary = summary == null ? "" : summary.strip();
        findings = findings == null ? List.of()
                : findings.stream()
                        .filter(f -> f != null && !f.message().isBlank())
                        .sorted(Comparator.comparing(Finding::severity))
                        .toList();
    }
}
