package fr.takit.reviewer.application;

/** The caller or the application used its free reviews for the day. The message is safe to show. */
public class QuotaExceededException extends RuntimeException {

    public QuotaExceededException(String message) {
        super(message);
    }
}
