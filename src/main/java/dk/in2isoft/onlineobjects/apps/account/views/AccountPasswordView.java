package dk.in2isoft.onlineobjects.apps.account.views;

import dk.in2isoft.commons.jsf.AbstractView;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.services.PasswordRecoveryService;
import dk.in2isoft.onlineobjects.ui.Request;

public class AccountPasswordView extends AbstractView {

	private PasswordRecoveryService passwordRecoveryService;

	private User user;

	private String key;
	
	private boolean found;
	
	public void before(Request request) throws Exception {
		
		key = request.getString("key");
		if (Strings.isNotBlank(key)) {
			user = passwordRecoveryService.getUserByRecoveryKey(key, request);
			found = user!=null;
		}
	}
	
	public boolean isFound() {
		return found;
	}
	
	public User getUser() {
		return user;
	}
	
	public String getKey() {
		return key;
	}
	
	// Wiring...
	
	public void setPasswordRecoveryService(PasswordRecoveryService passwordRecoveryService) {
		this.passwordRecoveryService = passwordRecoveryService;
	}
}
