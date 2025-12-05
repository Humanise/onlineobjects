package dk.in2isoft.onlineobjects.modules.dispatch;

import java.io.File;
import java.io.IOException;

import javax.servlet.FilterChain;

import dk.in2isoft.onlineobjects.services.ConfigurationService;
import dk.in2isoft.onlineobjects.services.DispatchingService;
import dk.in2isoft.onlineobjects.ui.Request;

public class In2iGuiResponder implements Responder {

	private ConfigurationService configurationService;


	public boolean applies(Request request) {
		String[] path = request.getFullPath();
		return path.length > 0 && path[0].equals("hui");
	}

	public void dispatch(Request request, FilterChain chain) throws IOException {

		String[] path = request.getFullPath();
		File file = configurationService.findExistingFile(path);
		if (file != null) {
			DispatchingService.pushFile(request.getResponse(), file);
		} else {
			request.notFound();
		}
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
}
