package dk.in2isoft.onlineobjects.apps.community;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import dk.in2isoft.onlineobjects.apps.ApplicationController;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.ui.Request;

public class CommunityController extends ApplicationController {

	public CommunityController() {
		super("community");
	}

	@Override
	public List<Locale> getLocales() {
		return null;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
	}

	@Override
	public void unknownRequest(Request request) throws IOException, EndUserException {
		String[] path = request.getLocalPath();
		String localPath = path.length > 0 ? path[0] : null;
		if (localPath!=null && localPath.endsWith(".html")) {
			localPath = null;
		}
		else if ("about".equals(localPath)) {
			localPath = null;
		}
		String url = configurationService.getApplicationContext("people", localPath, request);
		request.redirect(url);
	}
}
