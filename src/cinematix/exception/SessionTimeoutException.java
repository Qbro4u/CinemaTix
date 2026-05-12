package cinematix.exception;

public class SessionTimeoutException extends Exception {
    public SessionTimeoutException(String message) {
        super(message);
    }
}