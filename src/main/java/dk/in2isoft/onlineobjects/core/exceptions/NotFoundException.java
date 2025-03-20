package dk.in2isoft.onlineobjects.core.exceptions;

import javax.servlet.http.HttpServletResponse;

public class NotFoundException extends EndUserException {

	private static final long serialVersionUID = -351608398503987416L;

	public NotFoundException() {
		super();
	}

	@Override
	public int getHttpStatusCode() {
		return HttpServletResponse.SC_NOT_FOUND;
	}
	
	public NotFoundException(Class<?> type, Number id) {
		super("Object of type " + type.getSimpleName() + " and id=" + id + " was not found");
	}

	public NotFoundException(String message, String code) {
		super(message,code);
	}

	public NotFoundException(String message) {
		super(message);
	}

	public NotFoundException(String message, Throwable throwable) {
		super(message, throwable);
	}

	public NotFoundException(Throwable throwable) {
		super(throwable);
	}

}
