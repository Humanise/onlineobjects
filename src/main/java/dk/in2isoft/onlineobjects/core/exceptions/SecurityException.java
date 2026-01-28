package dk.in2isoft.onlineobjects.core.exceptions;

import jakarta.servlet.http.HttpServletResponse;

public class SecurityException extends EndUserException {

	private static final long serialVersionUID = 1449397281498175390L;

	public SecurityException() {
		super();
	}

	@Override
	public int getHttpStatusCode() {
		return HttpServletResponse.SC_UNAUTHORIZED;
	}

	public SecurityException(String arg0) {
		super(arg0);
	}

	public SecurityException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public SecurityException(Throwable arg0) {
		super(arg0);
	}

	public SecurityException(Error error) {
		super(error);
	}

}
