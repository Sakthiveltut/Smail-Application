package custom_exception;

public class AuthenticationFailedException extends Exception {
	
	public AuthenticationFailedException(String message) {
		super(message);
	}
}
