package dk.in2isoft.onlineobjects.core.exceptions;

public class EndUserException extends Exception {

	private static final long serialVersionUID = 1449397281498175390L;

	private String code;

	public EndUserException() {
		super();
	}

	public EndUserException(String message, String code) {
		super(message);
		this.code = code;
	}

	public EndUserException(Error error, Throwable e) {
		super(error.name(), e);
		this.code = error.name();
	}

	public EndUserException(Error error) {
		super(error.name());
		this.code = error.name();
	}

	public EndUserException(String message) {
		super(message);
	}

	public EndUserException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public EndUserException(Throwable throwable) {
		super(throwable);
	}

	public String getCode() {
		return code;
	}

	public int getHttpStatusCode() {
		return 500;
	}
	
	public static Exception findUserException(Exception ex) {
		if (ex instanceof EndUserException) {
			return ex;
		}
		Throwable cause = ex.getCause();
		while (cause!=null) {
			if (cause instanceof EndUserException) {
				return (Exception) cause;
			}
			cause = cause.getCause();
		}
		return ex;
	}
}
