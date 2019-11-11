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
import dk.in2isoft.onlineobjects.core.Pair;
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
	protected List<Pair<String[],Method>> exactMethodPaths = new ArrayList<>();
	protected List<Pair<Pattern,Method>> expressionMethodPaths = new ArrayList<>();
	protected List<Pair<String,Method>> methodsByName = new ArrayList<>();

	private String name;

	public AbstractController(String name) {
		this.name = name;
		Method[] methods = getClass().getDeclaredMethods();
		for (Method method : methods) {
			Path annotation = method.getAnnotation(Path.class);
			
			if (annotation!=null) {
				String[] exactly = annotation.exactly();
				if (exactly.length > 0) {
					exactMethodPaths.add(Pair.of(exactly, method));
				}
				else if (Strings.isNotBlank(annotation.expression())) {
					expressionMethodPaths.add(Pair.of(Pattern.compile(annotation.expression()), method));
				}
				else {
					methodsByName.add(Pair.of(method.getName(), method));
				}
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
		for (Pair<String[], Method> exact : exactMethodPaths) {
			if (request.testLocalPathFull(exact.getKey())) {
				invokeMethod(request, exact.getValue());
				return true;
			}			
		}
		String localPath = request.getLocalPathAsString();
		for (Pair<Pattern,Method> exp : expressionMethodPaths) {
			if (exp.getKey().matcher(localPath).matches()) {
				invokeMethod(request, exp.getValue());
				return true;
			}
		}
		for (Pair<String, Method> exact : methodsByName) {
			if (request.testLocalPathFull(exact.getKey())) {
				invokeMethod(request, exact.getValue());
				return true;
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
					throw new EndUserException(e);
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
}
