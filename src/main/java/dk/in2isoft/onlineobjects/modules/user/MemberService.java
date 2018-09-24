package dk.in2isoft.onlineobjects.modules.user;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.quartz.JobDataMap;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import dk.in2isoft.commons.lang.Files;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.apps.account.AccountController;
import dk.in2isoft.onlineobjects.core.EntitylistSynchronizer;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Pair;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.UserSession;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.Error;
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
import dk.in2isoft.onlineobjects.modules.scheduling.SchedulingService;
import dk.in2isoft.onlineobjects.modules.surveillance.SurveillanceService;
import dk.in2isoft.onlineobjects.services.ConfigurationService;
import dk.in2isoft.onlineobjects.services.EmailService;
import dk.in2isoft.onlineobjects.services.WebModelService;
import dk.in2isoft.onlineobjects.util.Messages;
import dk.in2isoft.onlineobjects.util.ValidationUtil;

public class MemberService {
	
	private static final Logger log = LogManager.getLogger(MemberService.class);
	
	private ModelService modelService;
	private WebModelService webModelService;
	private SecurityService securityService;
	private ConfigurationService configurationService;
	private EmailService emailService;
	private SchedulingService schedulingService;
	private SurveillanceService surveillanceService;
	
	private Multimap<String,Date> agreementConfigs = HashMultimap.create();

	public boolean isValidPassword(String password) {
		return ValidationUtil.isValidPassword(password);
	}

	public boolean isValidUsername(String username) {
		return ValidationUtil.isValidUsername(username);
	}

	public void validateNewMember(String username, String password, String email) throws IllegalRequestException, ModelException {
		if (!StringUtils.isNotBlank(username)) {
			throw new IllegalRequestException(Error.noUsername);
		}
		if (!isValidUsername(username)) {
			throw new IllegalRequestException(Error.invalidUsername);
		}
		if (!Strings.isNotBlank(password)) {
			throw new IllegalRequestException(Error.noPassword);
		}
		if (!isValidPassword(password)) {
			throw new IllegalRequestException(Error.invalidPassword);
		}
		if (!Strings.isNotBlank(email)) {
			throw new IllegalRequestException(Error.noEmail);
		}
		if (!isWellFormedEmail(email)) {
			throw new IllegalRequestException(Error.invalidEmail);
		}
		if (isUsernameTaken(username)) {
			throw new IllegalRequestException(Error.userExists);
		}
		if (isPrimaryEmailTaken(email)) {
			throw new IllegalRequestException(Error.emailExists);
		}
	}

	public boolean isWellFormedEmail(String email) {
		return ValidationUtil.isWellFormedEmail(email);
	}

	public Image getUsersProfilePhoto(User user, Privileged privileged) throws ModelException {
		return modelService.getChild(user, Relation.KIND_SYSTEM_USER_IMAGE, Image.class, privileged);
	}
	
	public Person getUsersPerson(User user, Privileged privileged) throws ModelException {
		return modelService.getChild(user, Relation.KIND_SYSTEM_USER_SELF, Person.class, privileged);
	}


	public User signUp(UserSession session, String username, String password, String fullName, String email) throws EndUserException {
		
		User user = createMember(session, username, password, fullName, email);
		
		markTermsAcceptance(user, user);

		securityService.changeUser(session, username, password);
		
		return user;
	}

	public User createMember(Privileged creator, String username,
			String password, String fullName, String email)
			throws IllegalRequestException, EndUserException, ModelException {
		surveillanceService.audit().info("Request to create user with username={}", username);
		if (creator==null) {
			throw new IllegalRequestException("Cannot create user without a creator");
		}
		if (email==null) {
			throw new IllegalRequestException("Cannot create member without an email");
		}
		// TODO: Figure out how to synch both username + email
		synchronized(email.intern()) {
			// TODO: Move this to the core
			validateNewMember(username, password, email);
			
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
			modelService.commit();
			scheduleHealthCheck(user);
			user = modelService.get(User.class, user.getId(), user);
			surveillanceService.audit().info("New member created with username={}", username);
			return user;
		}
	}

	public boolean isUsernameTaken(String username) {
		return modelService.getUser(username) != null;
	}

	public void deleteMember(User user, Privileged privileged) throws ModelException, SecurityException {
		if (user==null) {
			throw new SecurityException("Tried to delete null user");
		}
		surveillanceService.audit().info("Request to delete user={} by privileged={}", user.getUsername(), privileged.getIdentity());
		if (securityService.isCoreUser(user)) {
			throw new SecurityException("This type of member cannot be deleted");
		}
		if (!securityService.canDelete(user, privileged)) {
			throw new SecurityException("Deletion of user not allowed");
		}
		surveillanceService.audit().info("Starting to delete user={} by privileged={}", user.getUsername(), privileged.getIdentity());
		List<Entity> list = modelService.list(Query.of(Entity.class).as(user));
		// TODO check that an item is not shared with others
		for (Entity entity : list) {
			if (!entity.equals(user)) {
				modelService.deleteEntity(entity, privileged);
			}
		}
		modelService.deleteEntity(user, privileged);
		surveillanceService.audit().info("Completed deletion of user={} by privileged={}", user.getUsername(), privileged.getIdentity());
	}

	/**
	 * Will create, update or delete the primary email address
	 * @param user
	 * @param email
	 * @param privileged
	 * @return 
	 * @throws ModelException
	 * @throws SecurityException
	 * @throws IllegalRequestException 
	 */
	public EmailAddress changePrimaryEmail(User user, String email, Privileged privileged) throws ModelException, SecurityException, IllegalRequestException {
		surveillanceService.audit().info("Request to change email={} of user={} by privileged={}", email, user.getUsername(), privileged.getIdentity());
		email = email.trim();
		if (!isWellFormedEmail(email)) {
			throw new IllegalRequestException(Error.invalidEmail);
		}
		EmailAddress emailAddress = modelService.getChild(user, Relation.KIND_SYSTEM_USER_EMAIL, EmailAddress.class, privileged);
		if (emailAddress!=null) {
			if (email.equals(emailAddress.getAddress())) {
				return emailAddress;
				//throw new IllegalRequestException("The email is the same");
			}
			if (isPrimaryEmailTaken(email)) {
				throw new IllegalRequestException(Error.emailExists);
			}
			emailAddress.setAddress(email);
			emailAddress.setName(email);
			modelService.updateItem(emailAddress, privileged);
		} else {
			if (isPrimaryEmailTaken(email)) {
				throw new IllegalRequestException(Error.emailExists);
			}
			emailAddress = new EmailAddress();
			emailAddress.setAddress(email);
			emailAddress.setName(email);
			modelService.createItem(emailAddress, privileged);
			modelService.createRelation(user, emailAddress, Relation.KIND_SYSTEM_USER_EMAIL, privileged);
		}
		surveillanceService.audit().info("Completed change email={} of user={} by privileged={}", email, user.getUsername(), privileged.getIdentity());
		return emailAddress;
	}
	
	public boolean isPrimaryEmailTaken(String email) throws ModelException {
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
	
	public User getUserOfPrimaryEmail(EmailAddress email, Privileged privileged) throws ModelException {
		return modelService.getParent(email, Relation.KIND_SYSTEM_USER_EMAIL, User.class, privileged);
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
	
	public void scheduleHealthCheck(User user) {
		JobDataMap data = new JobDataMap();
		data.put(UserHealthCheckJob.USER_ID, user.getId());
		schedulingService.runJob(UserHealthCheckJob.NAME, UserHealthCheckJob.GROUP, data);
	}

	public void checkUserHealth(long userId) throws EndUserException {
		Privileged privileged = securityService.getAdminPrivileged();
		@NonNull
		User user = modelService.getRequired(User.class, userId, privileged);
		EmailAddress primaryEmail = getUsersPrimaryEmail(user, privileged);
		if (primaryEmail != null) {
			Date time = primaryEmail.getPropertyDateValue(Property.KEY_CONFIRMATION_TIME);
			if (time == null) {
				sendEmailConfirmation(user, privileged);
			}
		}
	}

	public void sendEmailConfirmation(User user, Privileged privileged) throws EndUserException {
		surveillanceService.audit().info("Request to send email confirmation to user={} by privileged={}", user.getUsername(), privileged.getIdentity());
		EmailAddress email = getUsersPrimaryEmail(user, privileged);
		if (email==null) {
			throw new IllegalRequestException("Tried sending email confirmation to user ("+user.getUsername()+") with no primary e-mail");
		}
		String name = getFullName(user, privileged);
		String random = Strings.generateRandomString(30);
		email.overrideFirstProperty(Property.KEY_EMAIL_CONFIRMATION_CODE, random);
		modelService.updateItem(email, user);
		StringBuilder url = new StringBuilder();
		String context = configurationService.getApplicationContext("account");
		url.append(context);
		// TODO: Get users preferred language
		url.append("/en/" + AccountController.EMAIL_CONFIRM_PATH + "?key=");
		url.append(random);
		url.append("&email=").append(email.getAddress());

		Map<String,Object> parms = new HashMap<>();
		parms.put("name", name);
		parms.put("url", url.toString());
		parms.put("base-url", "http://" + configurationService.getBaseUrl());
		String html = emailService.applyTemplate("dk/in2isoft/onlineobjects/emailconfirmation-template.html", parms);
		
		emailService.sendHtmlMessage("Confirm e-mail for OnlineObjects", html, email.getAddress(),name);
		email.overrideFirstProperty(Property.KEY_EMAIL_CONFIRMATION_REQUEST_TIME, new Date());
		surveillanceService.audit().info("Did send email confirmation to user={} via mail={}", user.getUsername(), email.getAddress());
	}

	private String getFullName(User user, Privileged privileged) throws ModelException {
		Person person = getUsersPerson(user, privileged);
		String name = user.getUsername();
		if (person != null && Strings.isNotBlank(person.getFullName())) {
			name = person.getFullName();
		}
		return name;
	}

	public void sendEmailChangeRequest(User user, String newEmail, Privileged privileged) throws EndUserException {
		surveillanceService.audit().info("Request to send email change request to user={} for email={} by privileged={}", user.getUsername(), newEmail, privileged.getIdentity());
		if (!isWellFormedEmail(newEmail)) {
			throw new IllegalRequestException(Error.invalidEmail);
		}
		EmailAddress email = getUsersPrimaryEmail(user, privileged);
		if (email!=null && newEmail.equals(email.getAddress())) {
			throw new IllegalRequestException(Error.emailSameAsCurrent);
		}
		if (isPrimaryEmailTaken(newEmail)) {
			throw new IllegalRequestException(Error.emailExists);
		}
		String key = Strings.generateRandomString(30) + "|" + newEmail;
		user.overrideFirstProperty(Property.KEY_EMAIL_CHANGE_CODE, key);
		modelService.updateItem(user, user);
		StringBuilder url = new StringBuilder();
		String context = configurationService.getApplicationContext("account");
		url.append(context);
		// TODO: Get users preferred language
		url.append("/en/" + AccountController.EMAIL_CONFIRM_CHANGE_PATH + "?key=");
		url.append(key);

		Map<String,Object> parms = new HashMap<>();
		String fullName = getFullName(user, privileged);
		parms.put("name", fullName);
		parms.put("url", url.toString());
		parms.put("base-url", "http://" + configurationService.getBaseUrl());
		String html = emailService.applyTemplate("dk/in2isoft/onlineobjects/emailchange-template.html", parms);
		
		emailService.sendHtmlMessage("Confirm e-mail for OnlineObjects", html, newEmail, fullName);
		surveillanceService.audit().info("Did send email change request to user={} for email={} by privileged={}", user.getUsername(), newEmail, privileged.getIdentity());
	}

	public User performEmailChangeByKey(String key) throws ContentNotFoundException, IllegalRequestException, ModelException, SecurityException {
		String[] parts = key.split("\\|");
		if (parts.length != 2) {
			throw new IllegalRequestException();
		}
		String email = parts[1];
		if (!isWellFormedEmail(email)) {
			throw new IllegalRequestException(Error.invalidEmail);
		}
		Privileged admin = securityService.getAdminPrivileged();
		Query<User> query = Query.after(User.class).withCustomProperty(Property.KEY_EMAIL_CHANGE_CODE, key);
		User user = modelService.getFirst(query);
		if (user==null) {
			throw new ContentNotFoundException("A user with the key could not be found");
		}
		EmailAddress currentEmail = getUsersPrimaryEmail(user, admin);
		if (currentEmail != null && email.equals(currentEmail.getAddress())) {
			throw new IllegalRequestException("The e-mail was already changed");
		}
		EmailAddress emailAddress = changePrimaryEmail(user, email, admin);
		markCondifirmed(emailAddress, user);
		surveillanceService.audit().info("Changed email for user={} to email={} via key", user.getUsername(), email);
		return user;
	}

	public Pair<EmailAddress, String> findEmailByConfirmationKey(String key) throws ContentNotFoundException, ModelException, SecurityException {
		Privileged privileged = securityService.getAdminPrivileged();
		Query<EmailAddress> query = Query.after(EmailAddress.class).withCustomProperty(Property.KEY_EMAIL_CONFIRMATION_CODE, key);
		@Nullable
		EmailAddress email = modelService.getFirst(query);
		if (email == null) {
			throw new ContentNotFoundException("Could not find the email with the confirmation code: "+key);
		}
		User user = getUserOfPrimaryEmail(email, privileged);
		if (user == null) {
			throw new ContentNotFoundException("Could not find the user for the email with the confirmation code: "+key);
		}
		String name = user.getUsername();
		Person person = getUsersPerson(user, privileged);
		if (person != null) {
			name = person.getFullName();
		}
		return Pair.of(email, name);
	}

	public void markCondifirmed(EmailAddress email, Privileged privileged) throws SecurityException, ModelException {
		// TODO: Use key to make sure this is legal
		Privileged admin = securityService.getAdminPrivileged();
		email.overrideFirstProperty(Property.KEY_CONFIRMATION_TIME, new Date());
		modelService.updateItem(email, admin);
		surveillanceService.audit().info("Marked email={} confirmed for privileged={}", email, privileged.getIdentity());
	}

	public void markTermsAcceptance(User user, Privileged privileged) throws SecurityException, ModelException {
		user.overrideFirstProperty(Property.KEY_TERMS_ACCEPTANCE_TIME, new Date());
		modelService.updateItem(user, privileged);
		surveillanceService.audit().info("Marked terms accepted for user={}", user.getUsername());
	}

	public boolean hasAcceptedTerms(User user, Privileged privileged) throws SecurityException, ModelException, ContentNotFoundException {
		Date accepted = user.getPropertyDateValue(Property.KEY_TERMS_ACCEPTANCE_TIME);
		Optional<Date> latestAgreementDate = getLatestAgreementDate();
		if (accepted == null || !latestAgreementDate.isPresent()) return false;
		Instant latestTerms = latestAgreementDate.get().toInstant();
		return accepted!=null && latestTerms.isBefore(accepted.toInstant());
	}

	public List<Agreement> getAgreements(User user, Locale locale) {
		Messages msg = new Messages(Agreement.class);
		List<Agreement> agreements = new ArrayList<>();
		for (String key : agreementConfigs.keySet()) {
			Date date = agreementConfigs.get(key).stream().sorted((a,b) -> b.compareTo(a)).findFirst().orElse(null);
			Agreement agreement = new Agreement();
			agreement.setKey(key);
			agreement.setTitle(msg.get(key, locale));
			agreement.setDate(date.getTime());
			File file = getAgreement(locale, key, date);
			if (!file.exists()) {
				file = getAgreement(Locale.ENGLISH, key, date);
			}
			agreement.setContent(Files.readString(file, Strings.UTF8));
			agreements.add(agreement);
		}
		return agreements;
	}

	private File getAgreement(Locale locale, String key, Date date) {
		String fileName = key + "-" + DateFormatUtils.format(date, "yyyy-MM-dd") + "-" + locale.getLanguage() + ".html";
		File file = configurationService.getFile("WEB-INF","core","agreements", fileName);
		return file;
	}
	
	private Optional<Date> getLatestAgreementDate() {
		return agreementConfigs.values().stream().sorted((a,b) -> b.compareTo(a)).findFirst();
	}
	
	public void setAgreementConfigs(Map<String,List<String>> configs) {
		
		for (Entry<String, List<String>> entry : configs.entrySet()) {
			for (String dateStr : entry.getValue()) {
				try {
					agreementConfigs.put(entry.getKey(),org.apache.commons.lang3.time.DateUtils.parseDate(dateStr,"yyyy-MM-dd"));
				} catch (Exception e) {
					log.error("Error parsing date",e);
				}

			}
		}
		
	}

	// Wiring...
	
	
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

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
	
	public void setEmailService(EmailService emailService) {
		this.emailService = emailService;
	}
	
	public void setSchedulingService(SchedulingService schedulingService) {
		this.schedulingService = schedulingService;
	}

	public void setSurveillanceService(SurveillanceService surveillanceService) {
		this.surveillanceService = surveillanceService;
	}

}
