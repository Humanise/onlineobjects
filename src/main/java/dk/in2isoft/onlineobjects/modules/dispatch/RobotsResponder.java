package dk.in2isoft.onlineobjects.modules.dispatch;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletResponse;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.services.ConfigurationService;
import dk.in2isoft.onlineobjects.ui.Request;

public class RobotsResponder implements Responder {

	// private static Logger log = LogManager.getLogger(RobotsResponder.class);

	private ConfigurationService configurationService;

	public boolean applies(Request request) {
		String[] path = request.getFullPath();
		if (path.length != 1)
			return false;
		if (path[0].equals("robots.txt")) {
			return true;
		}
		if (path[0].equals("apple-app-site-association")
				&& Strings.isNotBlank(configurationService.getAppleAppSiteAssociation())) {
			return true;
		}
		return false;
	}

	public Boolean dispatch(Request request, FilterChain chain) throws IOException {
		String[] path = request.getFullPath();
		if (path.length != 1)
			return null;

		HttpServletResponse response = request.getResponse();
		if (path[0].equals("robots.txt")) {
			response.setContentType("text/plain");
			PrintWriter writer = response.getWriter();
			writer.println("User-agent: *");
			writer.println("Disallow:");
			writer.close();
			return true;
		}
		if (path[0].equals("apple-app-site-association")) {
			String out = "{\n" + 
				"  \"webcredentials\": {\n" + 
				"    \"apps\": [\n" + 
				"      \"" + configurationService.getAppleAppSiteAssociation() + "\"\n" + 
				"    ]\n" + 
				"  }\n" + 
				"}";
			response.setContentType("application/json");
			PrintWriter writer = response.getWriter();
			writer.print(out);
			writer.close();
			return true;			
		}
		return null;
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
}
