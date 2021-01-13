package dk.in2isoft.onlineobjects.modules.dispatch;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletResponse;

import dk.in2isoft.onlineobjects.services.ConfigurationService;
import dk.in2isoft.onlineobjects.ui.Request;

public class RobotsResponder implements Responder {

	// private static Logger log = LogManager.getLogger(RobotsResponder.class);

	private ConfigurationService configurationService;

	public boolean applies(Request request) {
		String path = request.getLocalPathAsString();
		if (path.equals("/robots.txt") || path.equals("/.well-known/apple-app-site-association") || path.equals("/apple-app-site-association")) {
			return true;
		}
		return false;
	}

	public void dispatch(Request request, FilterChain chain) throws IOException {
		String path = request.getLocalPathAsString();

		HttpServletResponse response = request.getResponse();
		if (path.equals("/robots.txt")) {
			response.setContentType("text/plain");
			try (PrintWriter writer = response.getWriter()) {
				writer.println("User-agent: *");
				writer.println("Disallow:");
			}
		}
		if (path.endsWith("/apple-app-site-association")) {
			String out = "{\n" + 
				"  \"webcredentials\": {\n" + 
				"    \"apps\": [\n" + 
				"      \"" + configurationService.getAppleAppSiteAssociation() + "\"\n" + 
				"    ]\n" + 
				"  }\n" + 
				"}";
			response.setContentType("application/json");
			try (PrintWriter writer = response.getWriter()) {
				writer.print(out);
			}
		}
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
}
