package dk.in2isoft.onlineobjects.modules.user;

import java.util.HashMap;
import java.util.Map;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.apps.account.AccountController;
import dk.in2isoft.onlineobjects.apps.people.PeopleController;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.UserSession;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
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
	
	public Invitation getInvitation(String code) {
		Query<Invitation> query = new Query<Invitation>(Invitation.class).withField(Invitation.FIELD_CODE, code);
		return modelService.search(query).getFirst();
	}

	public Invitation createAndSendInvitation(String name, String emailAddress, String message, User sender) throws EndUserException {
		// TODO: Maybe check if a user with the primary e-mail already exists
		Person person = new Person();
		person.setFullName(name);
		modelService.createItem(person, sender);
		
		EmailAddress email = new EmailAddress();
		email.setAddress(emailAddress);
		modelService.createItem(email, sender);
		
		Relation personEmail = new Relation(person,email);
		modelService.createItem(personEmail, sender);
		
		Invitation invitation = createInvitation(sender, person, message);
		sendInvitation(invitation);
		return invitation;
	}

	public Invitation createInvitation(User sender, Person invited, String message) throws ModelException, SecurityException {

		Invitation invitation = new Invitation();
		invitation.setCode(Strings.generateRandomString(40));
		invitation.setMessage(message);
		modelService.createItem(invitation, sender);

		// Create relation from user to invitation
		Relation userInvitation = new Relation(sender, invitation);
		userInvitation.setKind(Relation.KIND_INIVATION_INVITER);
		modelService.createItem(userInvitation, sender);

		// Create relation from invitation to person
		Relation invitationPerson = new Relation(invitation, invited);
		invitationPerson.setKind(Relation.KIND_INIVATION_INVITED);
		modelService.createItem(invitationPerson, sender);

		return invitation;
	}

	public void sendInvitation(Invitation invitation) throws EndUserException {
		Privileged privileged = securityService.getAdminPrivileged();
		Person person = modelService.getChild(invitation, Person.class, privileged);
		User inviter = modelService.getParent(invitation, Relation.KIND_INIVATION_INVITER, User.class, privileged);
		Person inviterPerson = modelService.getChild(inviter, Relation.KIND_SYSTEM_USER_SELF, Person.class, privileged);
		if (person == null) {
			throw new EndUserException("The invitation does not have a person associated");
		}
		EmailAddress mail = modelService.getChild(person, EmailAddress.class, privileged);
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
			String username, String email, String password) throws EndUserException {

		Invitation invitation = getInvitation(code);
		if (invitation == null) {
			throw new IllegalRequestException("Could not find invitation with code: " + code);
		}
		if (!Invitation.STATE_ACTIVE.equals(invitation.getState())) {
			throw new EndUserException("The invitation is not active. The state is: " + invitation.getState());
		}
		
		Person person;
		{
			Privileged admin = securityService.getAdminPrivileged();
			person = modelService.getChild(invitation, Person.class,admin);

			invitation.setState(Invitation.STATE_ACCEPTED);
			modelService.updateItem(invitation, admin);
		}
		
		User newUser = memberService.signUp(session, username, password, person.getFullName(), email);
		if (newUser.getIdentity() != session.getIdentity()) {
			throw new StupidProgrammerException("The new user has not been logged in");
		}
		
		// Create relation between user and invitation
		Relation invitaionUserRelation = new Relation(invitation, newUser);
		invitaionUserRelation.setKind(Relation.KIND_INIVATION_INVITED);
		modelService.createItem(invitaionUserRelation, session);

		// The new user should be able to see the invite
		modelService.grantPrivileges(invitation, session, true, false, false, securityService.getAdminPrivileged());

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
