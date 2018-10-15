package dk.in2isoft.onlineobjects.core;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.exceptions.Error;
import dk.in2isoft.onlineobjects.core.exceptions.ExplodingClusterFuckException;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Client;
import dk.in2isoft.onlineobjects.model.Item;
import dk.in2isoft.onlineobjects.model.LogEntry;
import dk.in2isoft.onlineobjects.model.LogLevel;
import dk.in2isoft.onlineobjects.model.LogType;
import dk.in2isoft.onlineobjects.model.Privilege;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.surveillance.SurveillanceService;
import dk.in2isoft.onlineobjects.modules.user.ClientInfo;
import dk.in2isoft.onlineobjects.services.ConfigurationService;
import dk.in2isoft.onlineobjects.services.PasswordRecoveryService;
import dk.in2isoft.onlineobjects.util.ValidationUtil;

public class SecurityService {
	
	private static final Logger log = LogManager.getLogger(SecurityService.class);

	public static final String ADMIN_USERNAME = "admin";
	public static final String PUBLIC_USERNAME = "public";
	
	public static Set<String> RESERVED_USERNAMES = Sets.newHashSet(ADMIN_USERNAME,PUBLIC_USERNAME);
	
	private ModelService modelService;
	private ConfigurationService configurationService;
	private SurveillanceService surveillanceService;
	private PasswordEncryptionService passwordEncryptionService;
	private PasswordRecoveryService passwordRecoveryService;

	private User publicUser;
	private Privileged adminPrivileged;

	/**
	 * Tries to change the user of a session
	 * @param userSession The session to change user on
	 * @param username The username
	 * @param password The password
	 * @return True if user was changed
	 */
	public boolean changeUser(UserSession userSession, String username, String password) {
		if (userSession==null || username==null || password==null) {
			throw new IllegalArgumentException("session, username or password is null");
		}
		User user = getUser(username, password);
		if (user!=null) {
			surveillanceService.audit().info("Changed to user={}", username);
			Set<Ability> abilities = getAbilities(user);
			userSession.setUser(user, abilities);
			log(user, LogType.logIn);
			return true;
		}
		return false;
	}
	
	private Set<Ability> getAbilities(User user) {
		Collection<String> properties = user.getPropertyValues(Property.KEY_ABILITY);
		return Ability.convert(properties);
	}

	public void changeUserBySecret(UserSession userSession, String secret) throws SecurityException {
		User user = getUserBySecret(secret);
		if (user==null) {
			throw new SecurityException("No user found with the secret");
		}
		log(user, LogType.logIn);
		surveillanceService.audit().info("Changed (via secret) to user={}", user.getUsername());
		userSession.setUser(user, getAbilities(user));
	}
	
	private void log(User user, LogType type) {
		LogEntry entry = new LogEntry();
		entry.setSubject(user.getId());
		entry.setTime(new Date());
		entry.setType(type);
		entry.setLevel(LogLevel.info);
		modelService.create(entry);
	}
	
	public User getUser(String username, String password) {
		User user = modelService.getUser(username);
		if (user==null) {
			return null;
		}
		if (Strings.isNotBlank(user.getSalt())) {
			if (passwordEncryptionService.authenticate(password, user.getPassword(), user.getSalt())) {
				return user;
			}
		}
		return null;
	}
	
	public void changePassword(String username, String existingPassword,String newPassword, Privileged privileged) throws SecurityException, ModelException, IllegalRequestException, ExplodingClusterFuckException {
		User user = getUser(username, existingPassword);
		if (user==null) {
			throw new IllegalRequestException(Error.incorrectCurrentPassword);
		}
		changePassword(user, newPassword, privileged);
	}

	private void changePassword(User user, String password, Privileged privileged) throws ExplodingClusterFuckException, SecurityException, ModelException, IllegalRequestException {
		if (!ValidationUtil.isValidPassword(password)) {
			throw new IllegalRequestException(Error.invalidNewPassword);
		}
		setSaltedPassword(user, password);
		user.removeProperties(Property.KEY_PASSWORD_RECOVERY_CODE);
		modelService.updateItem(user, privileged);
		surveillanceService.audit().info("Changed password of user={}", user.getUsername());
	}

	public void setPassword(User user, String password) throws ExplodingClusterFuckException, SecurityException {
		if (!user.isNew()) {
			throw new SecurityException("Cannot set password of persistent user");
		}
		setSaltedPassword(user, password);
		surveillanceService.audit().info("Set initial password on user={}", user.getId());
	}

	private void setSaltedPassword(User user, String password) throws ExplodingClusterFuckException {
		String salt = passwordEncryptionService.generateSalt();
		String encryptedPassword = passwordEncryptionService.getEncryptedPassword(password, salt);
		user.setPassword(encryptedPassword);
		user.setSalt(salt);
	}

	public void changePasswordUsingKey(String key, String password, UserSession session) throws ExplodingClusterFuckException, SecurityException, ModelException, IllegalRequestException {
		User user = passwordRecoveryService.getUserByRecoveryKey(key);
		if (user!=null) {
			Privileged admin = getAdminPrivileged();
			changePassword(user, password, admin);
			changeUser(session, user.getUsername(), password);
		}
	}
	
	public boolean logOut(UserSession userSession) {
		User user = modelService.getUser("public");
		if (user==null) {
			return false;
		} else {
			userSession.setUser(user, new HashSet<>());
			return true;
		}
	}

	public boolean isPublicView(Item item) {
		Privilege privilege = modelService.getPrivilege(item.getId(), getPublicUser().getId());
		if (privilege!=null) {
			return privilege.isView();
		}
		return false;
	}
	
	public Privilege getPrivilege(long id,Privileged priviledged) {
		return modelService.getPrivilege(id, priviledged.getIdentity());
	}
	
	public List<Privileged> expand(Privileged priviledged) {
		List<Privileged> privs = Lists.newArrayList(priviledged);
		User publicUser = getPublicUser();
		if (priviledged.getIdentity()!=publicUser.getIdentity()) {
			privs.add(publicUser);
		}
		return privs;
	}
	
	public boolean canView(Item item,Privileged privileged) {
		if (isAdminUser(privileged)) {
			return true;
		}
		List<Privileged> expand = expand(privileged);
		for (Privileged priv : expand) {
			if (canExactlyView(item, priv)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean canDelete(Item item,Privileged privileged) {
		if (item instanceof User) {
			User user = (User) item;
			if (isAdminUser(user)) {
				return false;
			}
			if (isPublicUser(user)) {
				return false;
			}
		}
		if (isPublicUser(privileged)) {
			return false;
		}
		if (isAdminUser(privileged)) {
			return true;
		}
		List<Privileged> expand = expand(privileged);
		for (Privileged priv : expand) {
			if (canExactlyDelete(item, priv)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean canModify(Item item,Privileged privileged) {
		if (isAdminUser(privileged)) {
			return true;
		}
		if (isPublicUser(privileged)) {
			return false;
		}
		List<Privileged> expand = expand(privileged);
		for (Privileged priv : expand) {
			if (canExactlyModify(item, priv)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean canExactlyView(Item item, Privileged privileged) {
		Privilege privilege = getPrivilege(item.getId(), privileged);
		if (privilege==null) {
			return false;
		} else {
			return privilege.isView();
		}
	}
	
	private boolean canExactlyDelete(Item item, Privileged privileged) {
		Privilege privilege = getPrivilege(item.getId(), privileged);
		if (privilege==null) {
			return false;
		} else {
			return privilege.isDelete();
		}
	}
	
	private boolean canExactlyModify(Item item, Privileged privileged) {
		Privilege privilege = getPrivilege(item.getId(), privileged);
		if (privilege==null) {
			return false;
		} else {
			return privilege.isAlter();
		}
	}
	
	public User getPublicUser() {
		if (publicUser==null) {
			publicUser = modelService.getUser(SecurityService.PUBLIC_USERNAME);
		}
		return publicUser;
	}

	public boolean isPublicUser(Privileged privileged) {
		User pub = getPublicUser();
		return pub!=null && privileged.getIdentity()==pub.getIdentity();
	}

	public Privileged getAdminPrivileged() {
		if (adminPrivileged==null) {
			User user = modelService.getUser(SecurityService.ADMIN_USERNAME);
			adminPrivileged = new DummyPrivileged(user.getId());
		}
		return adminPrivileged;
	}

	public boolean isAdminUser(Privileged privileged) {
		Privileged admin = getAdminPrivileged();
		return admin!=null && privileged.getIdentity() == admin.getIdentity();
	}

	public User getUserBySecret(String secret) {
		if (Strings.isBlank(secret)) {
			return null;
		}
		UserQuery q = new UserQuery();
		q.setSecret(secret);
		SearchResult<User> result = modelService.search(q);
		return result.getFirst();		
	}
	
	public String getSecret(ClientInfo info, User user) throws ModelException, SecurityException {
		String secret;
		Query<Client> q = Query.after(Client.class).withField("UUID", info.getUUID()).as(user).withPaging(0, 1);
		final Client client = modelService.getFirst(q);
		if (client!=null) {
			secret = client.getPropertyValue(Property.KEY_AUTHENTICATION_SECRET);
			syncClientInfo(info, client);
			modelService.updateItem(client, user);
		} else {
			secret = buildSecret();
			Client newClient = new Client();
			newClient.overrideFirstProperty(Property.KEY_AUTHENTICATION_SECRET, secret);
			syncClientInfo(info, newClient);
			newClient.setUUID(info.getUUID());
			modelService.createItem(newClient, user);
			modelService.createRelation(user, newClient, user);
		}
		return secret;
	}

	private void syncClientInfo(ClientInfo info, Client newClient) {
		newClient.overrideFirstProperty(Property.KEY_CLIENT_PLATFORM, info.getPlatform());
		newClient.overrideFirstProperty(Property.KEY_CLIENT_PLATFORM_VERSION, info.getPlatformVersion());
		newClient.overrideFirstProperty(Property.KEY_CLIENT_VERSION, info.getClientVersion());
		newClient.overrideFirstProperty(Property.KEY_CLIENT_BUILD, info.getClientBuild());
		newClient.overrideFirstProperty(Property.KEY_CLIENT_HARDWARE, info.getHardware());
		newClient.overrideFirstProperty(Property.KEY_CLIENT_HARDWARE_VERSION, info.getHardwareVersion());
		newClient.setName(info.getNickname());
	}

	private User getInitialUser() {
		if (configurationService.isDevelopmentMode()) {
			String developmentUser = configurationService.getDevelopmentUser();
			if (Strings.isNotBlank(developmentUser)) {
				User user = modelService.getUser(developmentUser);
				if (user!=null) {
					return user;
				}
			}
		}
		return getPublicUser();
	}

	public void makePublicVisible(Item item, Privileged privileged) throws SecurityException, ModelException {
		modelService.grantPrivileges(item, getPublicUser(), true, false, false, privileged);
	}
	
	public void makePublicHidden(Item item, Privileged privileged) throws SecurityException {
		if (!canModify(item, privileged)) {
			throw new SecurityException("The user cannot make this non public");
		}
		User publicUser = getPublicUser();
		modelService.removePrivileges(item, publicUser, privileged);
	}

	public void grantPublicView(Item item, boolean view, Privileged granter) throws ModelException, SecurityException {
		modelService.grantPrivileges(item, getPublicUser(), view, false, false, granter);		
	}

	public void grantFullPrivileges(Item item, Privileged user, Privileged granter) throws ModelException, SecurityException {
		modelService.grantPrivileges(item, user, true, true, true, granter);
	}

	public UserSession ensureUserSession(HttpSession session) {
		if (session.getAttribute(UserSession.SESSION_ATTRIBUTE) == null) {
			log.trace("Creating new user session");
			session.setAttribute(UserSession.SESSION_ATTRIBUTE, new UserSession(getInitialUser()));
		}
		return UserSession.get(session);
	}


	public boolean canChangeUsername(User user) {
		return !SecurityService.RESERVED_USERNAMES.contains(user.getUsername());
	}
/*
	public String getSecret(User user) throws ModelException, SecurityException {
		User reloaded = modelService.get(User.class, user.getId(), user);
		if (reloaded!=null) {
			String secret = reloaded.getPropertyValue(Property.KEY_AUTHENTICATION_SECRET);
			if (Strings.isBlank(secret)) {
				secret = Strings.generateRandomString(50);
				reloaded.overrideFirstProperty(Property.KEY_AUTHENTICATION_SECRET, secret);
				modelService.updateItem(reloaded, user);
			}
			return secret;
		}
		return null;
	}*/
/*
	public String generateNewSecret(User user) throws ModelException, SecurityException {
		User reloaded = modelService.get(User.class, user.getId(), user);
		if (reloaded!=null) {
			String secret = buildSecret();
			reloaded.overrideFirstProperty(Property.KEY_AUTHENTICATION_SECRET, secret);
			modelService.updateItem(reloaded, user);
			return secret;
		}
		return null;
	}
*/	
	public String buildSecret() {
		return Strings.generateRandomString(50);
	}
	
	

	public boolean isCoreUser(User user) {
		return SecurityService.ADMIN_USERNAME.equals(user.getUsername()) || SecurityService.PUBLIC_USERNAME.equals(user.getUsername());
	}

	public void randomDelay() {
		try {
			int time = (int) (Math.random() * 1000 + 1500);
			Thread.sleep(time);
		} catch (InterruptedException e) {}
	}

	// Wiring...

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
	
	public void setSurveillanceService(SurveillanceService surveillanceService) {
		this.surveillanceService = surveillanceService;
	}
	
	public void setPasswordEncryptionService(PasswordEncryptionService passwordEncryptionService) {
		this.passwordEncryptionService = passwordEncryptionService;
	}
	
	public void setPasswordRecoveryService(PasswordRecoveryService passwordRecoveryService) {
		this.passwordRecoveryService = passwordRecoveryService;
	}



}
