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

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.util.RestUtil;
import dk.in2isoft.onlineobjects.core.Pair;
import dk.in2isoft.onlineobjects.core.Path;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.StupidProgrammerException;
import dk.in2isoft.onlineobjects.services.ConfigurationService;

public abstract class AbstractController {

	protected ConfigurationService configurationService;
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

	public void unknownRequest(Request request) throws IOException, EndUserException {
		for (Pair<String[], Method> exact : exactMethodPaths) {
			if (request.testLocalPathFull(exact.getKey())) {
				invokeMothod(request, exact.getValue());
				return;
				
			}			
		}
		for (Pair<Pattern,Method> exp : expressionMethodPaths) {
			if (exp.getKey().matcher(request.getLocalPathAsString()).matches()) {
				invokeMothod(request, exp.getValue());
				return;
			}
		}
		for (Pair<String, Method> exact : methodsByName) {
			if (request.testLocalPathFull(exact.getKey())) {
				invokeMothod(request, exact.getValue());
				return;
				
			}			
		}
		throw new ContentNotFoundException("The content could not be found");
	}

	public RequestDispatcher getDispatcher(Request request) {
		ServletContext context = request.getRequest().getSession().getServletContext();
		String localPath = request.getLocalPathAsString();
		String jsfPath = null;
		for (Map.Entry<Pattern, String> entry : jsfMatchers.entrySet()) {
			if (entry.getKey().matcher(localPath).matches()) {
				jsfPath = entry.getValue();
				break;
			}
		}
		if (jsfPath==null) {
			StringBuilder filePath = new StringBuilder();
			filePath.append(File.separator).append("jsf");
			filePath.append(File.separator).append(name);
			String[] path = request.getLocalPath();
			for (String item : path) {
				filePath.append(File.separator).append(item);
			}
			jsfPath = filePath.toString().replaceAll("\\.html", ".xhtml");
		}
		File file = new File(configurationService.getBasePath() + jsfPath);
		if (file.exists() && file.isFile()) {
			return context.getRequestDispatcher("/faces"+jsfPath);
		}
		return null;
	}

	private void invokeMothod(Request request, Method method) throws IOException, StupidProgrammerException, EndUserException {
		try {
			Object result = method.invoke(this, new Object[] { request });
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
}
