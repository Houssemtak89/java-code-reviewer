package fr.takit.reviewer.application;

import fr.takit.reviewer.domain.CodeSubmission;
import fr.takit.reviewer.domain.Review;

/** Port to whatever reviews the code. The LLM adapter implements it; tests use a fake. */
public interface CodeReviewer {

    /**
     * @param submission   the code, already checked against the size rules
     * @param visitorToken an API key given by the visitor, or null to use the application's own
     */
    Review review(CodeSubmission submission, String visitorToken);
}
