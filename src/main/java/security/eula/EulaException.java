package security.eula;

public class EulaException extends Exception {
	public EulaException(String message){
		super(message);
	}

	public EulaException(String message, Throwable cause){
		super(message, cause);
	}
}
