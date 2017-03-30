package dk.in2isoft.onlineobjects.modules.user;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.EntitylistSynchronizer;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.UserSession;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.EmailAddress;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.PhoneNumber;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.services.WebModelService;
import dk.in2isoft.onlineobjects.util.ValidationUtil;

public class MemberService {
	
	private ModelService modelService;
	private WebModelService webModelService;
	private SecurityService securityService;

	public void validateNewMember(String username, String password) throws IllegalRequestException {
		if (!StringUtils.isNotBlank(username)) {
			throw new IllegalRequestException("Username is not provided","noUsername");
		}
		if (!ValidationUtil.isValidUsername(username)) {
			throw new IllegalRequestException("Username contains invalid characters","invalidUsername");
		}
		if (!Strings.isNotBlank(password)) {
			throw new IllegalRequestException("Password is not provided","noPassword");
		}
		if (!ValidationUtil.isValidPassword(password)) {
			throw new IllegalRequestException("Password is not valid","invalidPassword");
		}
	}

	public void validateNewMember(String username, String password, String fullName, String email) throws IllegalRequestException {
		validateNewMember(username, password);
		if (!Strings.isNotBlank(fullName)) {
			throw new IllegalRequestException("Name is not provided","noName");
		}
		if (!Strings.isNotBlank(email)) {
			throw new IllegalRequestException("Email is not provided","noEmail");
		}
		if (!ValidationUtil.isWellFormedEmail(email)) {
			throw new IllegalRequestException("The email address is invalid","invalidEmail");
		}
	}

	public Image getUsersProfilePhoto(User user, Privileged privileged) throws ModelException {
		return modelService.getChild(user, Relation.KIND_SYSTEM_USER_IMAGE, Image.class, privileged);
	}
	
	public Person getUsersPerson(User user, Privileged privileged) throws ModelException {
		return modelService.getChild(user, Relation.KIND_SYSTEM_USER_SELF, Person.class, privileged);
	}


	public User signUp(UserSession session, String username, String password, String fullName, String email) throws EndUserException {

		User user = createMember(session, username, password, fullName, email);

		securityService.changeUser(session, username, password);

		return user;
	}

	public User createMember(Privileged creator, String username,
			String password, String fullName, String email)
			throws IllegalRequestException, EndUserException, ModelException {
		if (creator==null) {
			throw new IllegalRequestException("Cannot create user without a creator");
		}
		
		// TODO: Move this to the core
		validateNewMember(username, password, fullName, email);
		
		User existing = modelService.getUser(username);
		if (existing != null) {
			throw new IllegalRequestException("The user allready exists","userExists");
		}
		if (isPrimaryEmailTaken(email)) {
			throw new IllegalRequestException("The e-mail is already in use", "emailExists");
		}

		// Create a user
		User user = new User();
		user.setUsername(username);
		securityService.setPassword(user, password);
		modelService.createItem(user, creator);

		// Make sure only the user has access to itself
		modelService.removePrivileges(user, creator, securityService.getAdminPrivileged());
		securityService.grantFullPrivileges(user, user, securityService.getAdminPrivileged());
		
		// Make sure we do not accidentally use it agin
		creator = null;

		// Create a person
		Person person = new Person();
		person.setFullName(fullName);
		modelService.createItem(person, user);
		
		// Create email
		EmailAddress emailAddress = new EmailAddress();
		emailAddress.setAddress(email);
		modelService.createItem(emailAddress, user);
		
		// Create relation between person and email
		modelService.createRelation(person, emailAddress, user);

		// Create relation between person and email
		modelService.createRelation(user, emailAddress, Relation.KIND_SYSTEM_USER_EMAIL, user);

		// Create relation between user and person
		modelService.createRelation(user, person, Relation.KIND_SYSTEM_USER_SELF, user);
		/*
		TODO Disabled web site creation for now
		// Create a web site
		WebSite site = new WebSite();
		site.setName(buildWebSiteTitle(fullName));
		modelService.createItem(site, user);
		securityService.makePublicVisible(site, user);

		// Create relation between user and web site
		modelService.createRelation(user, site,user);

		webModelService.createWebPageOnSite(site.getId(),ImageGallery.class, user);
		*/
		return user;
	}

	public void deleteMember(User user, Privileged privileged) throws ModelException, SecurityException {
		List<Entity> list = modelService.list(Query.of(Entity.class).as(user));
		// TODO check that an item is not shared with others
		for (Entity entity : list) {
			if (!entity.equals(user)) {
				modelService.deleteEntity(entity, privileged);
			}
		}
		modelService.deleteEntity(user, privileged);
	}

	/**
	 * Will create, update or delete the primary email address
	 * @param user
	 * @param email
	 * @param privileged
	 * @throws ModelException
	 * @throws SecurityException
	 * @throws IllegalRequestException 
	 */
	public void changePrimaryEmail(User user, String email, Privileged privileged) throws ModelException, SecurityException, IllegalRequestException {
		email = email.trim();
		if (!ValidationUtil.isWellFormedEmail(email)) {
			throw new IllegalRequestException("The email is not well formed: "+email);
		}
		EmailAddress emailAddress = modelService.getChild(user, Relation.KIND_SYSTEM_USER_EMAIL, EmailAddress.class, privileged);
		if (emailAddress!=null) {
			if (email.equals(emailAddress.getAddress())) {
				throw new IllegalRequestException("The email is the same");
			}
			if (isPrimaryEmailTaken(email)) {
				throw new IllegalRequestException("The email is taken");
			}
			emailAddress.setAddress(email);
			emailAddress.setName(email);
			modelService.updateItem(emailAddress, privileged);
		} else {
			emailAddress = new EmailAddress();
			emailAddress.setAddress(email);
			emailAddress.setName(email);
			modelService.createItem(emailAddress, privileged);
			modelService.createRelation(user, emailAddress, Relation.KIND_SYSTEM_USER_EMAIL, privileged);
		}
	}
	
	private boolean isPrimaryEmailTaken(String email) throws ModelException {
		return getUserByPrimaryEmail(email, securityService.getAdminPrivileged()) != null;
	}

	/**
	 * TODO: Optimize this
	 * @param email
	 * @param privileged
	 * @return
	 * @throws ModelException
	 */
	public User getUserByPrimaryEmail(String email, Privileged privileged) throws ModelException {
		Query<EmailAddress> query = Query.after(EmailAddress.class).withField(EmailAddress.ADDRESS_PROPERTY, email).orderByCreated();
		
		List<EmailAddress> list = modelService.list(query);
		for (EmailAddress emailAddress : list) {
			User user = modelService.getParent(emailAddress, Relation.KIND_SYSTEM_USER_EMAIL, User.class, privileged);
			if (user!=null) {
				return user;
			}
		}
		return null;
	}
	
	public EmailAddress getUsersPrimaryEmail(User user, Privileged privileged) throws ModelException {
		return modelService.getChild(user, Relation.KIND_SYSTEM_USER_EMAIL, EmailAddress.class, privileged);
	}

	/*
	private String buildWebSiteTitle(String fullName) {
		fullName = fullName.trim();
		if (fullName.endsWith("s")) {
			fullName+="'";
		} else {
			fullName+="'s";			
		}
		return fullName+" hjemmeside";
	}*/

	public UserProfileInfo build(Person person,Privileged priviledged) throws ModelException {
		UserProfileInfo info = new UserProfileInfo();
		info.setGivenName(person.getGivenName());
		info.setFamilyName(person.getFamilyName());
		info.setAdditionalName(person.getAdditionalName());
		info.setSex(person.getSex());
		info.setResume(person.getPropertyValue(Property.KEY_HUMAN_RESUME));
		info.setInterests(person.getPropertyValues(Property.KEY_HUMAN_INTEREST));
		info.setMusic(person.getPropertyValues(Property.KEY_HUMAN_FAVORITE_MUSIC));
		info.setMovies(person.getPropertyValues(Property.KEY_HUMAN_FAVORITE_MOVIE));
		info.setBooks(person.getPropertyValues(Property.KEY_HUMAN_FAVORITE_BOOK));
		info.setTelevisionPrograms(person.getPropertyValues(Property.KEY_HUMAN_FAVORITE_TELEVISIONPROGRAM));
		info.setEmails(modelService.getChildren(person, EmailAddress.class,priviledged));
		info.setPhones(modelService.getChildren(person, PhoneNumber.class,priviledged));
		info.setUrls(modelService.getChildren(person, InternetAddress.class,priviledged));
		return info;
	}
	
	public void save(UserProfileInfo info,Person person,Privileged priviledged) throws EndUserException {
		person.setGivenName(info.getGivenName());
		person.setAdditionalName(info.getAdditionalName());
		person.setFamilyName(info.getFamilyName());
		person.setSex(info.getSex());
		person.overrideFirstProperty(Property.KEY_HUMAN_RESUME, info.getResume());
		person.overrideProperties(Property.KEY_HUMAN_INTEREST, info.getInterests());
		person.overrideProperties(Property.KEY_HUMAN_FAVORITE_MUSIC, info.getMusic());
		person.overrideProperties(Property.KEY_HUMAN_FAVORITE_MOVIE, info.getMovies());
		person.overrideProperties(Property.KEY_HUMAN_FAVORITE_BOOK, info.getBooks());
		person.overrideProperties(Property.KEY_HUMAN_FAVORITE_TELEVISIONPROGRAM, info.getTelevisionPrograms());
		modelService.updateItem(person, priviledged);
		updateDummyEmailAddresses(person, info.getEmails(), priviledged);
		updateDummyPhoneNumbers(person, info.getPhones(), priviledged);
		updateDummyInternetAddresses(person, info.getUrls(), priviledged);
	}
	
	private void updateDummyEmailAddresses(Entity parent,List<EmailAddress> addresses, Privileged session) throws EndUserException {
		
		// Remove empty addresses
		for (Iterator<EmailAddress> i = addresses.iterator(); i.hasNext();) {
			EmailAddress emailAddress = i.next();
			if (!Strings.isNotBlank(emailAddress.getAddress())) {
				i.remove();
			}
		}
		
		List<EmailAddress> existing = modelService.getChildren(parent, EmailAddress.class, session);
		EntitylistSynchronizer<EmailAddress> sync = new EntitylistSynchronizer<EmailAddress>(existing,addresses);
		
		for (Entry<EmailAddress, EmailAddress> entry : sync.getUpdated().entrySet()) {
			EmailAddress original = entry.getKey();
			EmailAddress dummy = entry.getValue();
			original.setAddress(dummy.getAddress());
			original.setContext(dummy.getContext());
		}
		
		for (EmailAddress emailAddress : sync.getNew()) {
			modelService.createItem(emailAddress, session);
			modelService.createRelation(parent, emailAddress, session);
		}
		
		for (EmailAddress emailAddress : sync.getDeleted()) {
			modelService.deleteEntity(emailAddress, session);
		}
	}

	
	private void updateDummyPhoneNumbers(Entity parent,List<PhoneNumber> phones, Privileged priviledged) throws EndUserException {

		// Remove empty addresses
		for (Iterator<PhoneNumber> i = phones.iterator(); i.hasNext();) {
			PhoneNumber number = i.next();
			if (!Strings.isNotBlank(number.getNumber())) {
				i.remove();
			}
		}
		List<PhoneNumber> existing = modelService.getChildren(parent, PhoneNumber.class, priviledged);
		EntitylistSynchronizer<PhoneNumber> sync = new EntitylistSynchronizer<PhoneNumber>(existing,phones);
		
		for (Entry<PhoneNumber, PhoneNumber> entry : sync.getUpdated().entrySet()) {
			PhoneNumber original = entry.getKey();
			PhoneNumber dummy = entry.getValue();
			original.setNumber(dummy.getNumber());
			original.setContext(dummy.getContext());
		}
		
		for (PhoneNumber number : sync.getNew()) {
			modelService.createItem(number, priviledged);
			modelService.createRelation(parent, number, priviledged);
		}
		
		for (PhoneNumber number : sync.getDeleted()) {
			modelService.deleteEntity(number, priviledged);
		}
	}

	private void updateDummyInternetAddresses(Entity parent, List<InternetAddress> urls, Privileged priviledged) throws ModelException, SecurityException {

		// Remove empty addresses
		for (Iterator<InternetAddress> i = urls.iterator(); i.hasNext();) {
			InternetAddress address = i.next();
			if (!Strings.isNotBlank(address.getAddress())) {
				i.remove();
			}
		}
		List<InternetAddress> existing = modelService.getChildren(parent, InternetAddress.class, priviledged);
		EntitylistSynchronizer<InternetAddress> sync = new EntitylistSynchronizer<InternetAddress>(existing,urls);
		
		for (Entry<InternetAddress, InternetAddress> entry : sync.getUpdated().entrySet()) {
			InternetAddress original = entry.getKey();
			InternetAddress dummy = entry.getValue();
			original.setAddress(dummy.getAddress());
			original.setContext(dummy.getContext());
		}
		
		for (InternetAddress number : sync.getNew()) {
			modelService.createItem(number, priviledged);
			modelService.createRelation(parent, number, priviledged);
		}
		
		for (InternetAddress number : sync.getDeleted()) {
			modelService.deleteEntity(number, priviledged);
		}
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public ModelService getModelService() {
		return modelService;
	}

	public void setWebModelService(WebModelService webModelService) {
		this.webModelService = webModelService;
	}

	public WebModelService getWebModelService() {
		return webModelService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public SecurityService getSecurityService() {
		return securityService;
	}


}
