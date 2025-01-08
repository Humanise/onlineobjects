package dk.in2isoft.onlineobjects.modules.dispatch;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletResponse;

import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.StupidProgrammerException;
import dk.in2isoft.onlineobjects.services.ConfigurationService;
import dk.in2isoft.onlineobjects.services.DispatchingService;
import dk.in2isoft.onlineobjects.ui.AbstractController;
import dk.in2isoft.onlineobjects.ui.Request;

public class AbstractControllerResponder {

	protected ConfigurationService configurationService;

	public AbstractControllerResponder() {
		super();
	}

	protected boolean pushFile(String[] path, HttpServletResponse response) throws IOException {
		StringBuilder filePath = new StringBuilder();
		filePath.append(configurationService.getBasePath());
		for (int i = 0; i < path.length; i++) {
			filePath.append(File.separator);
			filePath.append(path[i]);
		}
		File file = new File(filePath.toString());

		if (file.exists() && !file.isDirectory()) {
			DispatchingService.pushFile(response, file);
			return true;
		}
		return pushFileLegacy(path, response);
	}
	
	private boolean pushFileLegacy(String[] path, HttpServletResponse response) throws IOException {
		StringBuilder filePath = new StringBuilder();
		filePath.append(configurationService.getBasePath());
		filePath.append(File.separator);
		filePath.append("WEB-INF");
		filePath.append(File.separator);
		filePath.append("apps");
		filePath.append(File.separator);
		filePath.append(path[1]);
		filePath.append(File.separator);
		filePath.append("web");
		for (int i = 2; i < path.length; i++) {
			filePath.append(File.separator);
			filePath.append(path[i]);
		}
		File file = new File(filePath.toString());

		if (file.exists() && !file.isDirectory()) {
			DispatchingService.pushFile(response, file);
			return true;
		}
		return false;
	}
	protected void invokeMothod(AbstractController controller, Request request, Method method) throws IOException, StupidProgrammerException, EndUserException {
		try {
			Object result = method.invoke(controller, new Object[] { request });
			Class<?> returnType = method.getReturnType();
			if (!returnType.equals(Void.TYPE)) {
				request.sendObject(result);
			}
			return;
		} catch (IllegalArgumentException e) {
			throw new StupidProgrammerException(e);
		} catch (IllegalAccessException e) {
			throw new EndUserException(e);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			if (cause instanceof EndUserException) {
				throw (EndUserException) cause;
			}
			else if (cause!=null) {
				throw new EndUserException(cause);
			} else {
				throw new EndUserException(e);
			}
		}
	}
	
	public final void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

}