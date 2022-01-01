package dk.in2isoft.onlineobjects.service.authentication;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.modules.user.MemberService;
import dk.in2isoft.onlineobjects.service.ServiceController;
import dk.in2isoft.onlineobjects.services.PasswordRecoveryService;

public abstract class AuthenticationControllerBase extends ServiceController {

	protected SecurityService securityService;
	protected MemberService memberService;
	protected PasswordRecoveryService passwordRecoveryService;
	protected ModelService modelService;

	
	public AuthenticationControllerBase() {
		super("authentication");
	}
	
	// Wiring...

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setMemberService(MemberService memberService) {
		this.memberService = memberService;
	}
	
	public void setPasswordRecoveryService(PasswordRecoveryService passwordRecoveryService) {
		this.passwordRecoveryService = passwordRecoveryService;
	}
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
}