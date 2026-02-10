package dk.in2isoft.onlineobjects.ui;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.util.RestUtil;
import dk.in2isoft.in2igui.FileBasedInterface;
import dk.in2isoft.onlineobjects.core.Path;
import dk.in2isoft.onlineobjects.core.View;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.NotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.StupidProgrammerException;
import dk.in2isoft.onlineobjects.services.ConfigurationService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

public abstract class AbstractController {

	protected ConfigurationService configurationService;
	protected HUIService huiService;

	protected List<Responder> responders = new ArrayList<>();

	private String name;

	public AbstractController(String name) {
		this.name = name;
		Method[] methods = getClass().getDeclaredMethods();
		for (Method method : methods) {
			Path path = method.getAnnotation(Path.class);

			if (path!=null) {
				String[] exactly = path.exactly();
				Responder responder = new Responder();
				responder.method = method;
				if (path.method() != dk.in2isoft.onlineobjects.core.Path.Method.NONE) {
					responder.httpMethod = path.method().name();
				}
				if (exactly.length > 0) {
					responder.path = exactly;
				}
				else if (Strings.isNotBlank(path.expression())) {
					responder.pattern = RestUtil.compile(path.expression());
				}
				else if (Strings.isNotBlank(path.of())) {
					responder.pattern = RestUtil.compile(path.of());
				}
				else if (Strings.isNotBlank(path.value())) {
					responder.pattern = RestUtil.compile(path.value());
				}
				else {
					responder.path = new String[] {method.getName()};
				}
				responders.add(responder);
			}
		}
	}

	public final String getName() {
		return name;
	}

	public String getLanguage(Request request) {
		return null;
	}

	public boolean handle(Request request) throws StupidProgrammerException, IOException, EndUserException {
		String localPath = request.getLocalPathAsString();
		for (Responder responder : responders) {
			if (responder.httpMethod != null) {
				if (!responder.httpMethod.equals(request.getRequest().getMethod())) {
					continue;
				}
			}
			if (responder.path != null) {
				if (request.testLocalPathFull(responder.path)) {
					invokeMethod(request, responder.method);
					return true;
				}
			}
			else if (responder.pattern != null) {
				if (responder.pattern.matcher(localPath).matches()) {
					invokeMethod(request, responder.method);
					return true;
				}
			}
		}
		return false;
	}

	private boolean dispatchToJSF(Request request, String path) throws IOException, EndUserException {
		ServletContext context = request.getRequest().getServletContext();

		String urlPath = getDimension() + "/" + getName() + "/" + path;
		File file = new File(configurationService.getBasePath() + File.separator + urlPath);
		if (!file.exists()) {
			urlPath = "jsf/" + getName() + "/" + path;
		}

		RequestDispatcher dispatcher = context.getRequestDispatcher("/faces/" + urlPath);
		request.getResponse().setContentType("text/html");
		request.getResponse().setCharacterEncoding("UTF-8");
		try {
			dispatcher.forward(request.getRequest(), request.getResponse());
			return true;
		} catch (ServletException e) {
			Exception userException = EndUserException.findUserException(e);
			if (userException instanceof EndUserException) {
				throw (EndUserException) userException;
			} else {
				throw new EndUserException(e.getCause());
			}
		}
	}

	public void unknownRequest(Request request) throws IOException, EndUserException {
		throw new NotFoundException("Not found");
	}

	public RequestDispatcher getDispatcher(Request request) {
		return null;
	}

	private void invokeMethod(Request request, Method method) throws IOException, StupidProgrammerException, EndUserException {
		try {
			Object result = method.invoke(this, new Object[] { request });
			Class<?> returnType = method.getReturnType();
			View view = method.getDeclaredAnnotation(View.class);
			if (view != null && view.ui().length > 0) {
				renderHUI(request, result, view.ui());
			}
			else if (view != null && view.jsf().length() > 0) {
				dispatchToJSF(request, view.jsf());
			}
			else if (view != null && view.value().length() > 0) {
				if (view.value().endsWith(".xhtml")) {
					dispatchToJSF(request, view.value());
				} else if (view.value().endsWith(".xml")) {
					renderHUI(request, result, view.value().split("/"));
				}
			}
			else if (!returnType.equals(Void.TYPE)) {
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

	private void renderHUI(Request request, Object result, String[] path)
			throws IOException {
		FileBasedInterface ui = new FileBasedInterface(getFile(path), huiService);
		if (result instanceof Map) {
			Map<?,?> params = (Map<?, ?>) result;
			for (Entry<?, ?> entry : params.entrySet()) {
				ui.setParameter(entry.getKey().toString(), entry.getValue());
			}
		}
		ui.render(request.getRequest(), request.getResponse());
	}

	public final File getFile(String... path) {
		File file = getFile(false, path);
		if (file.exists()) {
			return file;
		}
		return getFile(true, path);
	}

	public final File getFile(boolean webInf, String... path) {
		StringBuilder filePath = new StringBuilder();
		filePath.append(configurationService.getBasePath());
		filePath.append(File.separator);
		if (webInf) {
			filePath.append("WEB-INF");
			filePath.append(File.separator);
		}
		filePath.append(getDimension());
		filePath.append(File.separator);
		filePath.append(getName());
		for (int i = 0; i < path.length; i++) {
			filePath.append(File.separator);
			filePath.append(path[i]);
		}
		return new File(filePath.toString());
	}


	protected abstract String getDimension();

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	public boolean logAccessExceptions() {
		return true;
	}

	public void setHuiService(HUIService huiService) {
		this.huiService = huiService;
	}

	private class Responder {
		String httpMethod;
		Method method;
		String[] path;
		Pattern pattern;
	}
}
