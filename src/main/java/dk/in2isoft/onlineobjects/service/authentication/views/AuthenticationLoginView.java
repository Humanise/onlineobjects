package dk.in2isoft.onlineobjects.service.authentication.views;

import java.util.Locale;

import dk.in2isoft.commons.jsf.AbstractView;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.UserSession;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.util.Messages;

public class AuthenticationLoginView extends AbstractView {
	
	private static final Messages messages = new Messages(AuthenticationLoginView.class);
	private ModelService modelService;
	private SecurityService securityService;
	private String currentUserName;
	private String redirect;
	private String message;
	private Locale locale;
	private boolean loggedIn;
	private String logOutLink;
	
	@Override
	protected void before(Request request) throws Exception {
		super.before(request);
		redirect = request.getString("redirect");
		UserSession session = request.getSession();
		User user = modelService.getUser(request);
		if (user != null) {
			currentUserName = user.getUsername();
		}
		loggedIn = !securityService.isPublicUser(session); 
		String action = request.getString("action");
		locale = request.getRequest().getLocale();
		if (!"da".equals(locale.getLanguage()) && !"en".equals(locale.getLanguage())) {
			locale = Locale.ENGLISH;
		}
		if (Strings.isNotBlank(action)) {
			message = messages.get("action_"+action, locale);
		}
		logOutLink = "/logout?redirect=" + request.getString("redirect");
	}
	
	public boolean isLoggedIn() {
		return loggedIn;
	}
	
	public String getCurrentUserName() {
		return currentUserName;
	}
	
	public String getRedirect() {
		return redirect;
	}
	
	public String getMessage() {
		return message;
	}
	
	public Locale getLocale() {
		return locale;
	}
	
	public String getLogOutLink() {
		return logOutLink;
	}
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
}
