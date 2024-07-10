package dk.in2isoft.onlineobjects.apps.developer;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import dk.in2isoft.onlineobjects.apps.ApplicationController;
import dk.in2isoft.onlineobjects.core.Path;
import dk.in2isoft.onlineobjects.core.View;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.ui.Request;

public class DeveloperController extends ApplicationController {
	
	public DeveloperController() {
		super("developer");
	}

	@Path(expression = "/")
	@View(jsf = "index.xhtml")
	public void front(Request request) {
	}

	@Path(exactly = {"components.html"})
	@View(jsf = "components.xhtml")
	public void components(Request request) {
	}

	@Path(exactly = {"jsf.html"})
	@View(jsf = "jsf.xhtml")
	public void jsf(Request request) {
	}

	@Path(exactly = {"finder.html"})
	@View(jsf = "finder.xhtml")
	public void finder(Request request) {}
	
	public List<Locale> getLocales() {
		return null;
	}
	
	@Override
	public boolean isAllowed(Request request) {
		return configurationService.isDevelopmentMode();
	}

	@Path(exactly={"test"})
	public void importVideo(Request request) throws IOException, EndUserException {
		throw new ContentNotFoundException();
	}
	
	@Path(exactly={"toggleSlow"})
	public String toggleSlowRequests(Request request) {
		configurationService.setSimulateSlowRequest(!configurationService.isSimulateSlowRequest());
		return "Slow requests are now:" + configurationService.isSimulateSlowRequest();
	}
	
	@Path(exactly={"toggleErrors"})
	public String toggleErrors(Request request) {
		configurationService.setSimulateSporadicServerError(!configurationService.isSimulateSporadicServerError());
		return "Sporadic server error simulation is now:" + configurationService.isSimulateSporadicServerError();
	}
	// Injection...

}
