package dk.in2isoft.onlineobjects.ui;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.util.RestUtil;
import dk.in2isoft.in2igui.FileBasedInterface;
import dk.in2isoft.onlineobjects.core.Path;
import dk.in2isoft.onlineobjects.core.View;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.StupidProgrammerException;
import dk.in2isoft.onlineobjects.services.ConfigurationService;

public abstract class AbstractController {

	protected ConfigurationService configurationService;
	protected HUIService huiService;

	protected Map<Pattern,String> jsfMatchers = new LinkedHashMap<Pattern, String>();
	protected List<Responder> responders = new ArrayList<>();

	private String name;

	public AbstractController(String name) {
		this.name = name;
		Method[] methods = getClass().getDeclaredMethods();
		for (Method method : methods) {
			Path annotation = method.getAnnotation(Path.class);
			
			if (annotation!=null) {
				String[] exactly = annotation.exactly();
				Responder responder = new Responder();
				responder.method = method;
				if (annotation.method().length() > 0) {
					responder.httpMethod = annotation.method().toUpperCase();
				}
				if (exactly.length > 0) {
					responder.path = exactly;
				}
				else if (Strings.isNotBlank(annotation.expression())) {
					responder.pattern = Pattern.compile(annotation.expression());
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

	protected void addJsfMatcher(String pattern,String path) {
		jsfMatchers.put(RestUtil.compile(pattern), "/jsf/"+getName()+"/"+path);
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
		for (Map.Entry<Pattern, String> entry : jsfMatchers.entrySet()) {
			if (entry.getKey().matcher(localPath).matches()) {
				ServletContext context = request.getRequest().getSession().getServletContext();
				RequestDispatcher dispatcher = context.getRequestDispatcher("/faces" + entry.getValue());
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
		}
		return false;
	}
	
	public void unknownRequest(Request request) throws IOException, EndUserException {
		throw new ContentNotFoundException("The content could not be found");
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
				FileBasedInterface ui = new FileBasedInterface(getFile(view.ui()), huiService);
				ui.render(request.getRequest(), request.getResponse());
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
	
	public final File getFile(String... path) {
		StringBuilder filePath = new StringBuilder();
		filePath.append(configurationService.getBasePath());
		filePath.append(File.separator);
		filePath.append("WEB-INF");
		filePath.append(File.separator);
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
