package dk.in2isoft.onlineobjects.apps.account.views;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.model.EmailAddress;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.ui.AbstractManagedBean;
import dk.in2isoft.onlineobjects.ui.Request;

public class AccountSettingsView extends AbstractManagedBean {

	private ModelService modelService;
	
	private Person person;

	private User user;

	private EmailAddress email;

	private String primaryEmail;
	
	private boolean allowed;
	
	private String language;
	
	public void before(Request request) throws Exception {
		user = request.getSession().getUser();
		if (user.getUsername().equals(SecurityService.PUBLIC_USERNAME)) {
			return;
		}
		// Reload the user to get the latest properties and ensure it exists
		user = modelService.get(User.class, user.getId(), user);
		// TODO (jm) The user may not have access to itself!!!
		if (user==null) {
			return;
		}
		allowed = true;
		person = modelService.getChild(user, Relation.KIND_SYSTEM_USER_SELF, Person.class, user);
		email = modelService.getChild(user, Relation.KIND_SYSTEM_USER_EMAIL, EmailAddress.class, user);
		if (email!=null) {
			primaryEmail = email.getAddress();
		}
		language = request.getLanguage(); 
	}
	
	public String getSecret() {
		return user.getPropertyValue(Property.KEY_AUTHENTICATION_SECRET);
	}
	
	public boolean isAllowed() {
		return allowed;
	}
	
	public User getUser() {
		return user;
	}
	
	public Person getPerson() {
		return person;
	}
	
	public String getPrimaryEmail() {
		return primaryEmail;
	}

	public String getLanguage() {
		return language;
	}
	
	// Wiring...

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
}
