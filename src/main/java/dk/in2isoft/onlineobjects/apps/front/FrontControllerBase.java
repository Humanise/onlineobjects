package dk.in2isoft.onlineobjects.apps.front;

import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;

import dk.in2isoft.onlineobjects.apps.ApplicationController;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.ui.Request;

public abstract class FrontControllerBase extends ApplicationController {
	
	private SecurityService securityService;

	public FrontControllerBase() {
		super("front");
		addJsfMatcher("/", "front.xhtml");
		addJsfMatcher("/<language>", "front.xhtml");
		addJsfMatcher("/<language>/<folder>/<integer>", "entity.xhtml");
	}

	public List<Locale> getLocales() {
		return Lists.newArrayList(new Locale("en"),new Locale("da"));
	}
	
	@Override
	public boolean isAllowed(Request request) {
		User user = request.getSession().getUser();
		if (securityService.isPublicUser(user)) {
			String[] path = request.getLocalPath();
			if (path.length > 1) {
				return !false;
			}
		}
		return true;
	}
	
	@Override
	public String getMountPoint() {
		return "www";
	}

	@Override
	public String getLanguage(Request request) {
		String[] path = request.getLocalPath();
		if (path.length>0) {
			return path[0];
		}
		return super.getLanguage(request);
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
}