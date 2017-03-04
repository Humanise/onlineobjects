package dk.in2isoft.onlineobjects.remoting;

import org.apache.commons.lang.StringUtils;

import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.services.PasswordRecoveryService;
import dk.in2isoft.onlineobjects.ui.AbstractRemotingFacade;

public class SecurityRemotingFacade extends AbstractRemotingFacade {
	
	private PasswordRecoveryService passwordRecoveryService;
	private SecurityService securityService;

	public boolean changeUser(String username, String password) throws EndUserException {
		if (!StringUtils.isNotBlank(username)) {
			throw new IllegalRequestException("Username is blank","usernameIsBlank");
		}
		if (!StringUtils.isNotBlank(password)) {
			throw new IllegalRequestException("Password is blank","passwordIsBlank");
		}
		return securityService.changeUser(getUserSession(), username, password);
	}

	public boolean logOut() throws EndUserException {
		return securityService.logOut(getUserSession());
	}
	
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public SecurityService getSecurityService() {
		return securityService;
	}

	public void setPasswordRecoveryService(PasswordRecoveryService passwordRecoveryService) {
		this.passwordRecoveryService = passwordRecoveryService;
	}

	public PasswordRecoveryService getPasswordRecoveryService() {
		return passwordRecoveryService;
	}
}
