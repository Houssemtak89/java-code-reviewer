package fr.takit.reviewer.adapter.web;

import fr.takit.reviewer.config.ReviewerProperties;
import fr.takit.reviewer.domain.CodeSubmission;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** What the page shows about the setup: the provider, the model and the limits. */
@RestController
class InfoController {

    private final Info info;

    InfoController(ReviewerProperties properties,
                   @Value("${spring.ai.openai.chat.options.model}") String model) {
        this.info = new Info(properties.providerName(), model, CodeSubmission.MAX_LINES,
                properties.reviewsPerCallerPerDay());
    }

    @GetMapping("/api/info")
    Info info() {
        return info;
    }

    record Info(String provider, String model, int maxLines, int freeReviewsPerDay) {
    }
}
