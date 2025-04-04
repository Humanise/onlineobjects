package dk.in2isoft.onlineobjects.apps.account;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

import dk.in2isoft.onlineobjects.apps.ApplicationController;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.modules.user.InvitationService;
import dk.in2isoft.onlineobjects.modules.user.MemberService;
import dk.in2isoft.onlineobjects.ui.Request;

public abstract class AccountControllerBase extends ApplicationController {

	public static final String EMAIL_CONFIRM_PATH = "confirm";
	public static final String EMAIL_CONFIRM_CHANGE_PATH = "confirm-email-change";

	protected SecurityService securityService;
	protected InvitationService invitationService;
	protected MemberService memberService;
	
	public AccountControllerBase() {
		super(AccountController.MOUNT);
	}
	
	@Override
	public boolean isAllowed(Request request) {
		String path = request.getLocalPathAsString();
		if (Pattern.matches("^/(da|en)/(signup|confirm|confirm\\-email\\-change$|password|agreements)", path)) {
			return true;
		}
		if ("/changePasswordUsingKey".equals(path)) {
			return true;
		}
		if (path.startsWith("/status")) {
			return true;
		}
		return !securityService.isPublicUser(request.getSession());
	}
	
	@Override
	public boolean askForUserChange(Request request) {
		return true;
	}

	public List<Locale> getLocales() {
		return Lists.newArrayList(new Locale("en"),new Locale("da"));
	}

	@Override
	public String getLanguage(Request request) {
		String[] path = request.getLocalPath();
		if (path.length>0 && ("da".equals(path[0]) || "en".equals(path[0]))) {
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

	public void setMemberService(MemberService memberService) {
		this.memberService = memberService;
	}
}