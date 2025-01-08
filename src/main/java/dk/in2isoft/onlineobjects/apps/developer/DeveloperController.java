package dk.in2isoft.onlineobjects.apps.developer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dk.in2isoft.onlineobjects.apps.ApplicationController;
import dk.in2isoft.onlineobjects.core.Path;
import dk.in2isoft.onlineobjects.core.View;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.services.DispatchingService;
import dk.in2isoft.onlineobjects.ui.Request;

public class DeveloperController extends ApplicationController {
	
	public DeveloperController() {
		super("developer");
	}

	@Path(expression = "/")
	@View(jsf = "index.xhtml")
	public void front(Request request) {
		request.setVariable("test", "hest");
	}

	@Path(exactly = {"components.html"})
	@View(jsf = "components.xhtml")
	public void components(Request request) {
	}

	@Path(exactly = {"jsf.html"})
	@View(jsf = "jsf.xhtml")
	public void jsf(Request request) {
	}
	
	@Path(exactly = {"users"})
	@View(jsf = "users.xhtml")
	public void users(Request request) {
	}

	@Path(exactly = {"hui-test"})
	@View(ui = {"hui.xml"})
	public void hui(Request request) {
	}

	@Path(exactly = {"settings"})
	@View(ui = {"settings.xml"})
	public void settings(Request request) {
	}


	@Path(exactly = "mail")
	public void mail(Request request) throws IOException {
		File file = new File(configurationService.getTempDir(), request.getString("address")+".html");
		DispatchingService.pushFile(request.getResponse(), file);
	}

	public List<Locale> getLocales() {
		return null;
	}
	
	@Override
	public boolean isAllowed(Request request) {
		return configurationService.isDevelopmentMode() || configurationService.isTestMode();
	}

	@Path(exactly={"not-found"})
	public void throwNotFound(Request request) throws IOException, EndUserException {
		throw new ContentNotFoundException();
	}
	
	@Path(exactly={"bad-request"})
	public void throwBadRequest(Request request) throws IOException, EndUserException {
		throw new IllegalRequestException();
	}

	@Path(exactly={"settings", "data"}, method = "POST")
	public void saveSettings(Request request) {
		request.optionalBoolean("errors").ifPresent(value -> {
			configurationService.setSimulateSporadicServerError(value);
		});
		request.optionalBoolean("slow").ifPresent(value -> {
			configurationService.setSimulateSlowRequest(value);
		});
	}

	@Path(exactly={"settings", "data"}, method = "GET")
	public Map<String,Object> readSettings(Request request) {
		return Map.of(
			"errors", configurationService.isSimulateSporadicServerError(),
			"slow", configurationService.isSimulateSlowRequest()
		);
	}
}
