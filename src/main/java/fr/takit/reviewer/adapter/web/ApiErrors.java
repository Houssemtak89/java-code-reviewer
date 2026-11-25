package fr.takit.reviewer.adapter.web;

import fr.takit.reviewer.application.QuotaExceededException;
import fr.takit.reviewer.application.ReviewUnavailableException;
import fr.takit.reviewer.domain.InvalidSubmissionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps the application's errors to RFC 9457 problem details with a message the page can show. */
@RestControllerAdvice
class ApiErrors {

    @ExceptionHandler(InvalidSubmissionException.class)
    ProblemDetail invalid(InvalidSubmissionException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail missing(MethodArgumentNotValidException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Paste some Java code to review.");
    }

    @ExceptionHandler(QuotaExceededException.class)
    ProblemDetail quota(QuotaExceededException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, e.getMessage());
    }

    @ExceptionHandler(ReviewUnavailableException.class)
    ProblemDetail unavailable(ReviewUnavailableException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }
}
