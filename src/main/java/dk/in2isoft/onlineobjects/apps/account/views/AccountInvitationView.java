package dk.in2isoft.onlineobjects.apps.account.views;

import org.apache.commons.lang.StringEscapeUtils;

import dk.in2isoft.commons.jsf.AbstractView;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.model.EmailAddress;
import dk.in2isoft.onlineobjects.model.Invitation;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.user.InvitationService;
import dk.in2isoft.onlineobjects.ui.Request;


public class AccountInvitationView extends AbstractView {

	private ModelService modelService;
	private InvitationService invitationService;
	private SecurityService securityService;

	private Invitation invitation;
	private User inviterUser;
	private Person inviterPerson;
	private Person person;
	private EmailAddress email;
	private String code;

	public void before(Request request) throws Exception {
		// TODO: Use a more safe perspective
		Operator admin = request.as(securityService.getAdminPrivileged());
		invitation = invitationService.getInvitation(getCode(), admin);
		if (invitation!=null) {
			inviterUser = modelService.getParent(invitation, null, User.class, admin);
			inviterPerson = modelService.getChild(inviterUser, Person.class, admin);
			person = modelService.getChild(invitation, Person.class, admin);
			email = modelService.getChild(person, EmailAddress.class, admin);
		}
		code = request.getString("code");
	}

	public String getFormattedMessage() {
		if (invitation !=null && invitation.getMessage()!=null) {
			return StringEscapeUtils.escapeHtml(invitation.getMessage()).replaceAll("\\n", "<br/>");
		}
		return null;
	}

	public String getNewUsername() {
		String givenName = person.getGivenName();
		if (givenName!=null) {
			return givenName.toLowerCase();
		}
		return "";
	}

	public Invitation getInvitation() {
		return invitation;
	}

	public Person getInviterPerson() {
		return inviterPerson;
	}

	public String getEmail() {
		return email.getAddress();
	}

	public Person getPerson() {
		return person;
	}

	public User getInviterUser() {
		return inviterUser;
	}

	public String getCode() {
		return code;
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public void setInvitationService(InvitationService invitationService) {
		this.invitationService = invitationService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
}
