package dk.in2isoft.onlineobjects.modules.dispatch;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import dk.in2isoft.onlineobjects.apps.ApplicationController;
import dk.in2isoft.onlineobjects.core.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.EndUserException;
import dk.in2isoft.onlineobjects.core.IllegalRequestException;
import dk.in2isoft.onlineobjects.model.Application;
import dk.in2isoft.onlineobjects.services.ConfigurationService;
import dk.in2isoft.onlineobjects.services.DispatchingService;
import dk.in2isoft.onlineobjects.ui.Request;

public class ApplicationResponder implements Responder {

	private static Logger log = Logger.getLogger(ApplicationResponder.class);
	private static final Class<?>[] args = { Request.class };
	private Multimap<String, Pattern> mappings;
	
	private ConfigurationService configurationService;
	private HashMap<String, ApplicationController> controllers;
	
	public ApplicationResponder() {
		List<Application> apps = Lists.newArrayList();
		{
			Application app = new Application();
			app.setName("words");
			app.addProperty(Application.PROPERTY_URL_MAPPING, "words.onlineobjects.com");
			apps.add(app);
		}
		{
			Application app = new Application();
			app.setName("photos");
			app.addProperty(Application.PROPERTY_URL_MAPPING, "photos.onlineobjects.com");
			apps.add(app);
		}
		{
			Application app = new Application();
			app.setName("community");
			app.addProperty(Application.PROPERTY_URL_MAPPING, ".*");
			apps.add(app);
		}

		mappings = LinkedHashMultimap.create();

		for (Application app : apps) {
			for (String reg : app.getPropertyValues(Application.PROPERTY_URL_MAPPING)) {
				Pattern p = Pattern.compile(reg);
				mappings.put(app.getName(), p);
			}
		}
	}
	
	public boolean applies(Request request) {
		return true;
	}
	
	public Boolean dispatch(Request request, FilterChain chain) throws IOException, EndUserException {
		
		String[] path = request.getFullPath();
		if (path.length>1 && path[0].equals("app")) {
			request.setLocalContext((String[]) ArrayUtils.subarray(path, 0, 2));
			callApplication(path[1], request);
		} else {
			String appName = resolveMapping(request);
			if (appName == null) {
				DispatchingService.displayError(request, new EndUserException("Not found"));
				return null;
			} else {
				callApplication(appName, request);
			}
		}
		return true;
	}
	
	private ApplicationController getApplicationController(Request request, String name) {
		return controllers.get(name);
	}

	private String resolveMapping(Request request) {
		String domainName = request.getDomainName();
		for (Entry<String, Pattern> mapping : mappings.entries()) {
			Pattern pattern = mapping.getValue();
			Matcher matcher = pattern.matcher(domainName);
			if (matcher.matches()) {
				return mapping.getKey();
			}
		}
		return null;
	}

	private void callApplication(String application, Request request) throws IOException, EndUserException {
		request.setApplication(application);
		ApplicationController controller = getApplicationController(request,application);
		String[] path = request.getLocalPath();
		try {
			if (controller == null) {
				throw new ContentNotFoundException("Application not found");
			}
			if (!controller.isAllowed(request)) {
				throw new IllegalRequestException("Not allowed");
			}
			String language = controller.getLanguage(request);
			if (language!=null) {
				request.setLanguage(language);
			}
			RequestDispatcher dispatcher = controller.getDispatcher(request);
			if (dispatcher != null) {
				request.getResponse().setContentType("text/html");
				request.getResponse().setCharacterEncoding("UTF-8");
				dispatcher.forward(request.getRequest(), request.getResponse());
			} else {
				if (path.length > 0) {
					try {
						boolean called = callApplicationMethod(controller, path[0], request);
						if (!called) {
							String[] filePath = new String[] { "app", application };
							if (!pushFile((String[]) ArrayUtils.addAll(filePath, path), request.getResponse())) {
								controller.unknownRequest(request);
							}
						}
					} catch (InvocationTargetException e) {
						DispatchingService.displayError(request, e);
					}
				} else {
					controller.unknownRequest(request);
				}
			}
		} catch (ServletException e) {
			DispatchingService.displayError(request, e);
		}
	}

	private boolean callApplicationMethod(ApplicationController controller, String methodName, Request request)
			throws EndUserException, InvocationTargetException {
		try {
			Method method = controller.getClass().getDeclaredMethod(methodName, args);
			boolean accessible = method.isAccessible();
			if (!accessible)
				return false;
			method.invoke(controller, new Object[] { request });
			return true;
		} catch (IllegalAccessException e) {
			return false;
		} catch (NoSuchMethodException e) {
			return false;
		}
	}


	private boolean pushFile(String[] path, HttpServletResponse response) {
		boolean success = false;
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
		// log.info(file.getAbsolutePath());
		if (file.exists() && !file.isDirectory()) {
			try {
				DispatchingService.pushFile(response, file);
				success = true;
			} catch (Exception e) {
				log.error(e.toString(), e);
			}
		}
		return success;
	}
	
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
	
	public void setApplicationControllers(List<ApplicationController> controllers) {
		this.controllers = new HashMap<String,ApplicationController>();
		for (ApplicationController controller : controllers) {
			String name = controller.getClass().getSimpleName();
			name = name.toLowerCase().substring(0,name.indexOf("Controller"));
			this.controllers.put(name, controller);
		}
	}
}