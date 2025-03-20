package dk.in2isoft.onlineobjects.services;

import java.util.HashMap;
import java.util.Map;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.SearchResult;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.Error;
import dk.in2isoft.onlineobjects.core.exceptions.BadRequestException;
import dk.in2isoft.onlineobjects.model.EmailAddress;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.surveillance.SurveillanceService;
import dk.in2isoft.onlineobjects.modules.user.MemberService;

public class PasswordRecoveryService {

	private EmailService emailService;
	private ModelService modelService;
	private ConfigurationService configurationService;
	private MemberService memberService;
	private SecurityService securityService;
	private SurveillanceService surveillanceService;
	
	public boolean sendRecoveryMail(String usernameOrEmail, Operator operator) throws EndUserException {
		if (!memberService.isValidUsername(usernameOrEmail) && !memberService.isWellFormedEmail(usernameOrEmail)) {
			throw new BadRequestException(Error.invalidUsernameOrEmail);
		}
		User user = modelService.getUser(usernameOrEmail, operator);
		if (user==null) {
			user = memberService.getUserByPrimaryEmail(usernameOrEmail, operator.as(securityService.getAdminPrivileged()));
		}
		if (user!=null) {
			return sendRecoveryMail(user, operator);
		}
		return false;
	}
	
	public boolean sendRecoveryMail(User user, Operator operator) throws EndUserException {
		surveillanceService.audit().info("Request to recover password for user={}", user.getUsername());
		Privileged admin = securityService.getAdminPrivileged();
		operator = operator.as(admin);
		EmailAddress usersPrimaryEmail = memberService.getUsersPrimaryEmail(user, operator);
		Person person = memberService.getUsersPerson(user, operator);
		if (usersPrimaryEmail!=null && person!=null) {
			return sendRecoveryMail(user, person, usersPrimaryEmail, operator);			
		}
		return false;
	}
	
	public boolean sendRecoveryMail(User user, Person person, EmailAddress email, Operator operator) throws EndUserException {
		String random = Strings.generateRandomString(30);
		user.overrideFirstProperty(Property.KEY_PASSWORD_RECOVERY_CODE, random);
		operator = operator.as(securityService.getAdminPrivileged());
		modelService.update(user, operator);
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
		surveillanceService.audit().info("Did send e-mail to recover password for user={}", user.getUsername());
		return true;
	}

	public User getUserByRecoveryKey(String key, Operator operator) {
		Query<User> query = Query.of(User.class).withCustomProperty(Property.KEY_PASSWORD_RECOVERY_CODE, key);
		SearchResult<User> result = modelService.search(query, operator);
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
	
	public void setSurveillanceService(SurveillanceService surveillanceService) {
		this.surveillanceService = surveillanceService;
	}
}
