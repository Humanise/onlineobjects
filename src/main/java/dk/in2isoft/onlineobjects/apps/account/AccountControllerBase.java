package dk.in2isoft.onlineobjects.apps.account;

import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;

import dk.in2isoft.onlineobjects.apps.ApplicationController;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.modules.user.InvitationService;
import dk.in2isoft.onlineobjects.ui.Request;

public abstract class AccountControllerBase extends ApplicationController {

	protected SecurityService securityService;
	protected InvitationService invitationService;
	
	public AccountControllerBase() {
		super(AccountController.MOUNT);
		addJsfMatcher("/", "front.xhtml");
		addJsfMatcher("/invitation", "invitation.xhtml");
		addJsfMatcher("/<language>", "front.xhtml");
		addJsfMatcher("/<language>/password", "password.xhtml");
	}
	

	public List<Locale> getLocales() {
		return Lists.newArrayList(new Locale("en"),new Locale("da"));
	}

	@Override
	public String getLanguage(Request request) {
		String[] path = request.getLocalPath();
		if (path.length>0) {
			return path[0];
		}
		return super.getLanguage(request);
	}
	
	// Wiring...
	
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	
	public void setInvitationService(InvitationService invitationService) {
		this.invitationService = invitationService;
	}

}