package fr.takit.reviewer.domain;

import fr.takit.reviewer.domain.Finding.Category;
import fr.takit.reviewer.domain.Finding.Severity;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewTest {

    @Test
    void sortsFindingsMostSevereFirstAndDropsEmptyOnes() {
        Review review = new Review(" ok ", Arrays.asList(
                new Finding(Severity.MINOR, Category.READABILITY, 3, "Rename x", ""),
                null,
                new Finding(Severity.BLOCKER, Category.BUG, 7, "NPE when list is null", "Check for null"),
                new Finding(Severity.MAJOR, Category.DESIGN, 0, "   ", "")));

        assertThat(review.summary()).isEqualTo("ok");
        assertThat(review.findings()).extracting(Finding::severity)
                .containsExactly(Severity.BLOCKER, Severity.MINOR);
    }

    @Test
    void acceptsTheWordsModelsActuallyUse() {
        assertThat(Severity.from("high")).isEqualTo(Severity.MAJOR);
        assertThat(Severity.from("Critical")).isEqualTo(Severity.BLOCKER);
        assertThat(Severity.from("low")).isEqualTo(Severity.INFO);
        assertThat(Severity.from("whatever")).isEqualTo(Severity.MINOR);
        assertThat(Category.from("NullPointerException")).isEqualTo(Category.BUG);
        assertThat(Category.from("thread safety")).isEqualTo(Category.CONCURRENCY);
        assertThat(Category.from("security")).isEqualTo(Category.SECURITY);
        assertThat(Category.from("style")).isEqualTo(Category.READABILITY);
    }

    @Test
    void fillsGapsLeftByTheModel() {
        Finding finding = new Finding(null, null, -4, " message ", null);

        assertThat(finding.severity()).isEqualTo(Severity.MINOR);
        assertThat(finding.category()).isEqualTo(Category.READABILITY);
        assertThat(finding.line()).isZero();
        assertThat(finding.message()).isEqualTo("message");
        assertThat(finding.suggestion()).isEmpty();
        assertThat(new Review(null, null).findings()).isEmpty();
    }
}
