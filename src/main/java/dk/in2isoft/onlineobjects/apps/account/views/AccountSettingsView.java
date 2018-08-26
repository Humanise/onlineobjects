package dk.in2isoft.onlineobjects.apps.account.views;

import java.util.Date;
import java.util.Locale;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.apps.account.AccountController;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.model.EmailAddress;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.user.MemberService;
import dk.in2isoft.onlineobjects.ui.AbstractManagedBean;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.util.Dates;
import dk.in2isoft.onlineobjects.util.Messages;

public class AccountSettingsView extends AbstractManagedBean {

	private ModelService modelService;
	
	private Person person;

	private User user;

	private EmailAddress email;

	private String primaryEmail;
	
	private boolean allowed;
	
	private String language;

	private MemberService memberService;

	private boolean hasAcceptedTerms;

	private boolean emailConfirmed;

	private String emailConfirmationDate;
	
	public void before(Request request) throws Exception {
		Messages msg = new Messages(AccountController.class);
		user = modelService.get(User.class, request.getSession().getIdentity(), request.getSession());
		if (user==null) {
			return;
		}
		if (user.getUsername().equals(SecurityService.PUBLIC_USERNAME)) {
			return;
		}
		Locale locale = request.getLocale();
		allowed = true;
		person = modelService.getChild(user, Relation.KIND_SYSTEM_USER_SELF, Person.class, user);
		email = modelService.getChild(user, Relation.KIND_SYSTEM_USER_EMAIL, EmailAddress.class, user);
		Date emailConfirmationTime = null;
		if (email!=null) {
			primaryEmail = email.getAddress();
			emailConfirmationTime = email.getPropertyDateValue(Property.KEY_CONFIRMATION_TIME);
			emailConfirmed = emailConfirmationTime != null;
		}
		emailConfirmationDate = emailConfirmationTime==null ? msg.get("email_unconfirmed", locale) : Dates.formatDurationFromNow(emailConfirmationTime);
		language = request.getLanguage();
		this.hasAcceptedTerms = memberService.hasAcceptedTerms(user, user);
	}
	
	public boolean isHasAcceptedTerms() {
		return hasAcceptedTerms;
	}
	
	public String getSecret() {
		return user.getPropertyValue(Property.KEY_AUTHENTICATION_SECRET);
	}
	
	public boolean isAllowed() {
		return allowed;
	}
	
	public String getUsername() {
		return user.getUsername();
	}

	public String getFullName() {
		return Strings.isBlank(person.getFullName()) ? "None" : person.getFullName();
	}

	public String getFirstName() {
		return person.getGivenName();
	}

	public String getMiddleName() {
		return person.getAdditionalName();
	}

	public String getLastName() {
		return person.getFamilyName();
	}

	public String getPrimaryEmail() {
		return primaryEmail;
	}
	
	public boolean isEmailConfirmed() {
		return emailConfirmed;
	}
	
	public Object getEmailConfirmationDate() {
		return emailConfirmationDate;
	}

	public String getLanguage() {
		return language;
	}
	
	// Wiring...

	public void setMemberService(MemberService memberService) {
		this.memberService = memberService;
	}
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
}
