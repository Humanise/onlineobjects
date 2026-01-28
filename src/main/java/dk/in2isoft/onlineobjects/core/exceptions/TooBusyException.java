package dk.in2isoft.onlineobjects.core.exceptions;

import jakarta.servlet.http.HttpServletResponse;

public class TooBusyException extends EndUserException {

	private static final long serialVersionUID = 1449397281498175390L;

	public TooBusyException() {
		super("The server is currently too busy");
	}


	public TooBusyException(String message) {
		super(message);
	}

	@Override
	public int getHttpStatusCode() {
		return HttpServletResponse.SC_SERVICE_UNAVAILABLE;
	}
}
