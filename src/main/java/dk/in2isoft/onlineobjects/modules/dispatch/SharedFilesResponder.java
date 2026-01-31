package dk.in2isoft.onlineobjects.modules.dispatch;

import java.io.File;
import java.io.IOException;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.services.ConfigurationService;
import dk.in2isoft.onlineobjects.services.DispatchingService;
import dk.in2isoft.onlineobjects.ui.Request;
import jakarta.servlet.FilterChain;

public class SharedFilesResponder implements Responder {

	private ConfigurationService configurationService;
	private String[] context = new String[] { "shared" };

	@Override
	public boolean applies(Request request) {
		return request.testLocalPathStart("favicon.ico") || request.testLocalPathStart("robots.txt") || request.testLocalPathStart("ads.txt");
	}

	@Override
	public void dispatch(Request request, FilterChain chain) throws IOException {
		String[] path = Strings.merge(context, request.getLocalPath());
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
