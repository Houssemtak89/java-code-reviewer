package fr.takit.reviewer.application;

import fr.takit.reviewer.domain.CodeSubmission;
import fr.takit.reviewer.domain.Review;
import org.springframework.stereotype.Service;

/**
 * Use case: review a piece of Java code.
 * Requests that use the application's own token count against the daily quota of the caller;
 * requests that bring their own API key do not.
 */
@Service
public class ReviewService {

    private final CodeReviewer reviewer;
    private final DailyQuota quota;

    public ReviewService(CodeReviewer reviewer, DailyQuota quota) {
        this.reviewer = reviewer;
        this.quota = quota;
    }

    public Review review(String code, String caller, String visitorToken) {
        CodeSubmission submission = new CodeSubmission(code);
        boolean ownToken = visitorToken != null && !visitorToken.isBlank();
        if (!ownToken) {
            quota.consume(caller);
        }
        return reviewer.review(submission, ownToken ? visitorToken.strip() : null);
    }
}
