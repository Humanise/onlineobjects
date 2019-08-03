package dk.in2isoft.onlineobjects.modules.dispatch;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import dk.in2isoft.onlineobjects.apps.ApplicationController;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.services.DispatchingService;
import dk.in2isoft.onlineobjects.ui.Request;

public class ApplicationResponder extends AbstractControllerResponder implements Responder, InitializingBean {

	static Logger log = LogManager.getLogger(ApplicationResponder.class);
	private Map<String, String> mappings;
	
	private HashMap<String, ApplicationController> controllers;
	private DispatchingService dispatchingService;
		
	public void afterPropertiesSet() throws Exception {
		String domain = configurationService.getRootDomain();
				
		mappings = new HashMap<>();
		for (ApplicationController ctrl : controllers.values()) {
			mappings.put(ctrl.getMountPoint()+"."+domain, ctrl.getName());
		}

	}
	
	public boolean applies(Request request) {
		return true;
	}
	
	public Boolean dispatch(Request request, FilterChain chain) throws IOException, EndUserException {
		
		String[] path = request.getFullPath();
		if (configurationService.getRootDomain()==null && path.length>1 && path[0].equals("app")) {
			request.setLocalContext((String[]) ArrayUtils.subarray(path, 0, 2));
			callApplication(path[1], request);
		} else {
			String appName = resolveMapping(request);
			if (appName == null) {
				throw new ContentNotFoundException("Application not found");
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
		return mappings.get(domainName);
	}

	private void callApplication(String application, Request request) throws IOException, EndUserException {
		ApplicationController controller = getApplicationController(request,application);
		if (controller == null) {
			throw new ContentNotFoundException("Application not found: "+application);
		}
		String[] path = request.getLocalPath();
		try {
			request.setApplication(application);
			if (!controller.isAllowed(request)) {
				if (controller.askForUserChange(request)) {
					request.redirectFromBase("/service/authentication/?redirect="+request.getRequest().getRequestURI()+"&action=appAccessDenied");
					return;
				} else {
					throw new SecurityException("Application '"+application+"' denied access to user '"+request.getSession().getIdentity()+"'");
				}
			}
			String language = controller.getLanguage(request);
			if (language!=null) {
				request.setLanguage(language);
			}
			if (!controller.handle(request)) {
				RequestDispatcher dispatcher = controller.getDispatcher(request);
				if (dispatcher != null) {
					request.getResponse().setContentType("text/html");
					request.getResponse().setCharacterEncoding("UTF-8");
					dispatcher.forward(request.getRequest(), request.getResponse());
				} else if (path.length > 0) {
					String[] filePath = new String[] { "apps", application };
					if (!pushFile((String[]) ArrayUtils.addAll(filePath, path), request.getResponse())) {
						controller.unknownRequest(request);
					}
				} else {
					controller.unknownRequest(request);
				}
			}
		} catch (ServletException e) {
			dispatchingService.displayError(request, e);
		}
	}

	
	public void setDispatchingService(DispatchingService dispatchingService) {
		this.dispatchingService = dispatchingService;
	}
	
	public void setApplicationControllers(List<ApplicationController> controllers) {
		this.controllers = new HashMap<String,ApplicationController>();
		for (ApplicationController controller : controllers) {
			this.controllers.put(controller.getName(), controller);
		}
	}
}
