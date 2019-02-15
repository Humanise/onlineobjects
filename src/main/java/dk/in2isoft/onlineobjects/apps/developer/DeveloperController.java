package dk.in2isoft.onlineobjects.apps.developer;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import dk.in2isoft.onlineobjects.apps.ApplicationController;
import dk.in2isoft.onlineobjects.core.Path;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.ui.Request;

public class DeveloperController extends ApplicationController {
	
	public DeveloperController() {
		super("developer");
		addJsfMatcher("/components.html", "components.xhtml");
		addJsfMatcher("/", "index.xhtml");
	}
	
	public List<Locale> getLocales() {
		return null;
	}
	
	@Override
	public boolean isAllowed(Request request) {
		return configurationService.isDevelopmentMode();
	}

	@Path(start={"test"})
	public void importVideo(Request request) throws IOException, EndUserException {
		throw new ContentNotFoundException();
	}
	
	@Path(exactly={"toggleSlow"})
	public String toggleSlowRequests(Request request) {
		configurationService.setSimulateSlowRequest(!configurationService.isSimulateSlowRequest());
		return "Slow requests are now:" + configurationService.isSimulateSlowRequest();
	}
	
	// Injection...

}
