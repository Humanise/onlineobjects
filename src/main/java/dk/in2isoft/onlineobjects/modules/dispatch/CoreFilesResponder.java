package dk.in2isoft.onlineobjects.modules.dispatch;

import java.io.File;
import java.io.IOException;

import jakarta.servlet.FilterChain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dk.in2isoft.onlineobjects.services.ConfigurationService;
import dk.in2isoft.onlineobjects.services.DispatchingService;
import dk.in2isoft.onlineobjects.ui.Request;

public class CoreFilesResponder implements Responder {

	private static Logger log = LogManager.getLogger(CoreFilesResponder.class);

	private ConfigurationService configurationService;

	public boolean applies(Request request) {
		String[] path = request.getFullPath();
		return path.length > 0 && path[0].equals("core");
	}

	public void dispatch(Request request, FilterChain chain) throws IOException {
		String[] path = request.getFullPath();

		request.getResponse().addHeader("Access-Control-Allow-Origin", "*");
		File file = configurationService.findExistingFile(path);
		if (file != null) {
			DispatchingService.pushFile(request.getResponse(), file);
		} else {
			request.getResponse().setStatus(404);
		}
	}


	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
}
