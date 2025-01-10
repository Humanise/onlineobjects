package dk.in2isoft.onlineobjects.modules.dispatch;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;

import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.service.ServiceController;
import dk.in2isoft.onlineobjects.services.DispatchingService;
import dk.in2isoft.onlineobjects.ui.Request;

public class ServicesResponder extends AbstractControllerResponder implements Responder {
	
	private List<ServiceController> serviceControllers;
	
	public boolean applies(Request request) {
		String[] path = request.getFullPath();
		return path.length > 0 && path[0].equals("service");
	}
	
	public void dispatch(Request request, FilterChain chain) throws IOException, EndUserException {

		String[] path = request.getFullPath();
		request.setLocalContext((String[]) ArrayUtils.subarray(path, 0, 2));
		ServiceController controller = getServiceController(request,path[1]);
		if (controller == null) {
			throw new ContentNotFoundException("No controller found!");
		}
		String language = controller.getLanguage(request);
		if (language!=null) {
			request.setLanguage(language);
		}
		if (!controller.handle(request)) {
			if (!pushServiceFile(path, request.getResponse())) {
				controller.unknownRequest(request);
			}
		}
	}
	
	private boolean pushServiceFile(String[] path, HttpServletResponse response) {
		String[] full = path.clone();
		full[0] = "services";
		File file = configurationService.findExistingFile(full);
		if (file != null) {
			try {
				DispatchingService.pushFile(response, file);
				return true;
			} catch (Exception e) {
				//log.error(e.toString(), e);
			}
		}
		return false;
	}
	
	private ServiceController getServiceController(Request request, String name) {
		for (ServiceController controller : serviceControllers) {
			if (name.equals(controller.getName())) {
				return controller;
			}
		}
		return null;
	}
	
	public void setServiceControllers(List<ServiceController> serviceControllers) {
		this.serviceControllers = serviceControllers;
	}
}
