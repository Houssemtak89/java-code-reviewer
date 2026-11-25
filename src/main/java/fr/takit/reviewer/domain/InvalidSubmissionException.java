package fr.takit.reviewer.domain;

/** The submitted code breaks a size rule. The message is safe to show to the user. */
public class InvalidSubmissionException extends RuntimeException {

    public InvalidSubmissionException(String message) {
        super(message);
    }
}
