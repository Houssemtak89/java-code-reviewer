package fr.takit.reviewer.application;

/** The model could not answer (network, token, credit). The message is safe to show. */
public class ReviewUnavailableException extends RuntimeException {

    public ReviewUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
