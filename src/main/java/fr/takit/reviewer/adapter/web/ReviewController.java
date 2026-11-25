package fr.takit.reviewer.adapter.web;

import fr.takit.reviewer.application.ReviewService;
import fr.takit.reviewer.domain.Review;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
class ReviewController {

    /** Optional header: the visitor's own API key for the provider. Never logged, never stored. */
    static final String TOKEN_HEADER = "X-Api-Key";

    private final ReviewService service;

    ReviewController(ReviewService service) {
        this.service = service;
    }

    @PostMapping("/review")
    Review review(@Valid @RequestBody ReviewRequest request,
                  @RequestHeader(name = TOKEN_HEADER, required = false) String token,
                  HttpServletRequest http) {
        return service.review(request.code(), callerOf(http), token);
    }

    /** Behind the Hugging Face proxy, the visitor's address is the first one in X-Forwarded-For. */
    static String callerOf(HttpServletRequest http) {
        String forwarded = http.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].strip();
        }
        return http.getRemoteAddr();
    }

    record ReviewRequest(@NotBlank String code) {
    }
}
