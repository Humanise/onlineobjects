package dk.in2isoft.onlineobjects.services;

import java.util.HashMap;
import java.util.Map;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.SearchResult;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.EmailAddress;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.user.MemberService;

public class PasswordRecoveryService {

	private EmailService emailService;
	private ModelService modelService;
	private ConfigurationService configurationService;
	private MemberService memberService;
	private SecurityService securityService;
	
	public boolean sendRecoveryMail(String usernameOrEmail) throws EndUserException {
		User user = modelService.getUser(usernameOrEmail);
		if (user==null) {
			user = memberService.getUserByPrimaryEmail(usernameOrEmail, securityService.getAdminPrivileged());
		}
		if (user!=null) {
			return sendRecoveryMail(user);
		}
		return false;
	}
	
	public boolean sendRecoveryMail(User user) throws EndUserException {
		Privileged admin = securityService.getAdminPrivileged();
		EmailAddress usersPrimaryEmail = memberService.getUsersPrimaryEmail(user, admin);
		Person person = memberService.getUsersPerson(user, admin);
		if (usersPrimaryEmail!=null && person!=null) {
			return sendRecoveryMail(user, person, usersPrimaryEmail);			
		}
		return false;
	}
	
	public boolean sendRecoveryMail(User user, Person person, EmailAddress email) throws EndUserException {
		String random = Strings.generateRandomString(30);
		user.overrideFirstProperty(Property.KEY_PASSWORD_RECOVERY_CODE, random);
		modelService.updateItem(user, securityService.getAdminPrivileged());
		StringBuilder url = new StringBuilder();
		String context = configurationService.getApplicationContext("account");
		url.append(context);
		url.append("/en/password?key=");
		url.append(random);

		Map<String,Object> parms = new HashMap<String, Object>();
		parms.put("name", person.getFullName());
		parms.put("url",url.toString());
		parms.put("base-url", "http://" + configurationService.getBaseUrl());
		String html = emailService.applyTemplate("dk/in2isoft/onlineobjects/passwordrecovery-template.html", parms);
		
		emailService.sendHtmlMessage("Reset password for OnlineObjects", html, email.getAddress(),person.getName());
		return true;
	}

	public User getUserByRecoveryKey(String key) {

		SearchResult<User> result = modelService.search(Query.of(User.class).withCustomProperty(Property.KEY_PASSWORD_RECOVERY_CODE, key));
		return result.getFirst();
	}
	
	// Wiring...

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	public void setMemberService(MemberService memberService) {
		this.memberService = memberService;
	}
	
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
}
