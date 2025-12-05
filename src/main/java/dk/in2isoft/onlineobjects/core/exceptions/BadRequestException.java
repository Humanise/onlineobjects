package dk.in2isoft.onlineobjects.core.exceptions;

import javax.servlet.http.HttpServletResponse;

public class BadRequestException extends EndUserException {

	private static final long serialVersionUID = 1449397281498175390L;

	@Override
	public int getHttpStatusCode() {
		return HttpServletResponse.SC_BAD_REQUEST;
	}

	public BadRequestException() {
		super();
	}

	public BadRequestException(String message, String code) {
		super(message,code);
	}

	public BadRequestException(String message) {
		super(message);
	}

	public BadRequestException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public BadRequestException(Throwable throwable) {
		super(throwable);
	}

	public BadRequestException(Error error) {
		super(error);
	}

}
