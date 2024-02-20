package opium;

public class OpiumException extends Exception{
    public OpiumException(String message) {
        super(message);
    }

    public OpiumException() {
        super();
    }

    public OpiumException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpiumException(Throwable cause) {
        super(cause);
    }

    public OpiumException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
