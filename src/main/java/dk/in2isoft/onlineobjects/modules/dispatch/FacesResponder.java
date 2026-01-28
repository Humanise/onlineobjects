package dk.in2isoft.onlineobjects.modules.dispatch;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;

import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.ui.Request;

public class FacesResponder implements Responder {

	public boolean applies(Request request) {
		String localPathAsString = request.getRequest().getRequestURI();
		return localPathAsString.startsWith("/faces/");
	}

	public void dispatch(Request request, FilterChain chain) throws IOException, EndUserException {
		String localPath = request.getRequest().getRequestURI();
		RequestDispatcher requestDispatcher = request.getRequest().getRequestDispatcher(localPath);
		try {
			requestDispatcher.forward(request.getRequest(), request.getResponse());
		} catch (ServletException e) {
			throw new EndUserException(e);
		}
	}
}
