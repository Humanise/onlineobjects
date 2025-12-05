package dk.in2isoft.onlineobjects.modules.user;

import java.util.HashMap;
import java.util.Map;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.apps.account.AccountController;
import dk.in2isoft.onlineobjects.apps.people.PeopleController;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.UserSession;
import dk.in2isoft.onlineobjects.core.exceptions.BadRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.core.exceptions.StupidProgrammerException;
import dk.in2isoft.onlineobjects.model.EmailAddress;
import dk.in2isoft.onlineobjects.model.Invitation;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.services.ConfigurationService;
import dk.in2isoft.onlineobjects.services.EmailService;


public class InvitationService {

	private ModelService modelService;
	private EmailService emailService;
	private ConfigurationService configurationService;
	private MemberService memberService;
	private SecurityService securityService;

	public final static String INVITATION_TEMPLATE = "dk/in2isoft/onlineobjects/invitation-template.html";

	public Invitation getInvitation(String code, Operator operator) {
		Query<Invitation> query = new Query<Invitation>(Invitation.class).withField(Invitation.FIELD_CODE, code);
		return modelService.search(query, operator).getFirst();
	}

	public Invitation createAndSendInvitation(String name, String emailAddress, String message, User sender, Operator operator) throws EndUserException {
		// TODO: Maybe check if a user with the primary e-mail already exists
		operator = operator.as(sender);
		Person person = new Person();
		person.setFullName(name);
		modelService.create(person, operator);

		EmailAddress email = new EmailAddress();
		email.setAddress(emailAddress);
		modelService.create(email, operator);

		Relation personEmail = new Relation(person,email);
		modelService.create(personEmail, operator);

		Invitation invitation = createInvitation(sender, person, message, operator);
		sendInvitation(invitation, operator);
		return invitation;
	}

	public Invitation createInvitation(User sender, Person invited, String message, Operator operator) throws ModelException, SecurityException {

		Invitation invitation = new Invitation();
		invitation.setCode(Strings.generateRandomString(40));
		invitation.setMessage(message);
		modelService.create(invitation, operator);

		// Create relation from user to invitation
		Relation userInvitation = new Relation(sender, invitation);
		userInvitation.setKind(Relation.KIND_INIVATION_INVITER);
		modelService.create(userInvitation, operator);

		// Create relation from invitation to person
		Relation invitationPerson = new Relation(invitation, invited);
		invitationPerson.setKind(Relation.KIND_INIVATION_INVITED);
		modelService.create(invitationPerson, operator);

		return invitation;
	}

	public void sendInvitation(Invitation invitation, Operator operator) throws EndUserException {
		operator = operator.as(securityService.getAdminPrivileged());
		Person person = modelService.getChild(invitation, Person.class, operator);
		User inviter = modelService.getParent(invitation, Relation.KIND_INIVATION_INVITER, User.class, operator);
		Person inviterPerson = modelService.getChild(inviter, Relation.KIND_SYSTEM_USER_SELF, Person.class, operator);
		if (person == null) {
			throw new EndUserException("The invitation does not have a person associated");
		}
		EmailAddress mail = modelService.getChild(person, EmailAddress.class, operator);
		if (mail == null) {
			throw new EndUserException("The person does not have an email");
		}
		String inviterUrl = configurationService.getApplicationContext(PeopleController.MOUNT) + "/en/" + inviter.getUsername() + "/";
		String url = configurationService.getApplicationContext(AccountController.MOUNT) + "/invitation.html?code=" + invitation.getCode();
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("invited-name", person.getName());
		parameters.put("inviter-name", inviterPerson!=null ? inviterPerson.getFullName() : "");
		parameters.put("inviter-url", inviterUrl);
		parameters.put("invite-url", url);
		parameters.put("base-url", configurationService.getBaseUrl());
        String html = emailService.applyTemplate(INVITATION_TEMPLATE, parameters);

		emailService.sendHtmlMessage("Invitation til OnlineObjects", html, mail.getAddress(),person.getName());
	}

	public void signUpFromInvitation(UserSession session, String code,
			String username, String email, String password, Operator operator) throws EndUserException {

		Invitation invitation = getInvitation(code, operator);
		if (invitation == null) {
			throw new BadRequestException("Could not find invitation with code: " + code);
		}
		if (!Invitation.STATE_ACTIVE.equals(invitation.getState())) {
			throw new EndUserException("The invitation is not active. The state is: " + invitation.getState());
		}

		Operator admin = operator.as(securityService.getAdminPrivileged());
		Person person;
		{
			person = modelService.getChild(invitation, Person.class,admin);

			invitation.setState(Invitation.STATE_ACCEPTED);
			modelService.update(invitation, admin);
		}

		User newUser = memberService.signUp(session, username, password, person.getFullName(), email, operator);
		if (newUser.getIdentity() != session.getIdentity()) {
			throw new StupidProgrammerException("The new user has not been logged in");
		}

		// Create relation between user and invitation
		Relation invitaionUserRelation = new Relation(invitation, newUser);
		invitaionUserRelation.setKind(Relation.KIND_INIVATION_INVITED);
		modelService.create(invitaionUserRelation, operator);

		// The new user should be able to see the invite
		modelService.grantPrivileges(invitation, session, true, false, false, admin);

	}

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
