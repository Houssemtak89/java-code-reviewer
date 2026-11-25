package fr.takit.reviewer.application;

import fr.takit.reviewer.domain.CodeSubmission;
import fr.takit.reviewer.domain.InvalidSubmissionException;
import fr.takit.reviewer.domain.Review;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ReviewServiceTest {

    private final List<String> tokensSeen = new ArrayList<>();
    private final CodeReviewer reviewer = (submission, token) -> {
        tokensSeen.add(token);
        return new Review("fine", List.of());
    };
    private final DailyQuota quota = mock(DailyQuota.class);
    private final ReviewService service = new ReviewService(reviewer, quota);

    @Test
    void countsAReviewMadeWithTheApplicationToken() {
        service.review("class A {}", "1.2.3.4", null);

        verify(quota).consume("1.2.3.4");
        assertThat(tokensSeen).containsExactly((String) null);
    }

    @Test
    void doesNotCountAReviewMadeWithTheVisitorToken() {
        service.review("class A {}", "1.2.3.4", "  hf_visitor ");

        verify(quota, never()).consume(any());
        assertThat(tokensSeen).containsExactly("hf_visitor");
    }

    @Test
    void checksTheSizeBeforeSpendingTheQuota() {
        assertThatThrownBy(() -> service.review(" ", "1.2.3.4", null))
                .isInstanceOf(InvalidSubmissionException.class);
        verify(quota, never()).consume(any());
    }

    @Test
    void passesTheCodeUnchangedToTheReviewer() {
        List<CodeSubmission> seen = new ArrayList<>();
        new ReviewService((s, t) -> {
            seen.add(s);
            return new Review("", null);
        }, quota).review("int x = 1;", "c", null);

        assertThat(seen).extracting(CodeSubmission::code).containsExactly("int x = 1;");
    }
}
