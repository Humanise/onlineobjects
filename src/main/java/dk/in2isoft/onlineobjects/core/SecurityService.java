package dk.in2isoft.onlineobjects.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.exceptions.BadRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.Error;
import dk.in2isoft.onlineobjects.core.exceptions.ExplodingClusterFuckException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Client;
import dk.in2isoft.onlineobjects.model.Item;
import dk.in2isoft.onlineobjects.model.LogType;
import dk.in2isoft.onlineobjects.model.Privilege;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.surveillance.SurveillanceService;
import dk.in2isoft.onlineobjects.modules.user.ClientInfo;
import dk.in2isoft.onlineobjects.services.ConfigurationService;
import dk.in2isoft.onlineobjects.services.PasswordRecoveryService;
import dk.in2isoft.onlineobjects.ui.Request;
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
	public boolean changeUser(UserSession userSession, String username, String password, Operator operator) {
		if (userSession==null || username==null || password==null) {
			throw new IllegalArgumentException("session, username or password is null");
		}
		User user = getUser(username, password, operator);
		if (user!=null) {
			Set<Ability> abilities = getAbilities(user);
			userSession.setUser(user, abilities);
			surveillanceService.audit().info("Changed to user={}", username);
			surveillanceService.log(user, LogType.logIn);
			return true;
		}
		surveillanceService.audit().warn("Failed login request for username={}", username);
		return false;
	}

	public boolean changeUser(Request request, String username, String password, Operator operator) {
		boolean changed = changeUser(request.getSession(), username, password, operator);
		if (changed) {
			startSession(request);
		}
		return changed;
	}

	public void startSession(Request request) {
		HttpSession session = request.getRequest().getSession();
		session.setAttribute(UserSession.SESSION_ATTRIBUTE, request.getSession());
	}

	private Set<Ability> getAbilities(User user) {
		Collection<String> properties = user.getPropertyValues(Property.KEY_ABILITY);
		return Ability.convert(properties);
	}

	public User getUser(String username, String password, Operator operator) {
		User user = modelService.getUser(username, operator);
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

	public void changePassword(String username, String existingPassword,String newPassword, Operator operator) throws SecurityException, ModelException, BadRequestException, ExplodingClusterFuckException {
		User user = getUser(username, existingPassword, operator);
		if (user==null) {
			throw new BadRequestException(Error.incorrectCurrentPassword);
		}
		changePassword(user, newPassword, operator);
	}

	private void changePassword(User user, String password, Operator operator) throws ExplodingClusterFuckException, SecurityException, ModelException, BadRequestException {
		if (!ValidationUtil.isValidPassword(password)) {
			throw new BadRequestException(Error.invalidNewPassword);
		}
		setSaltedPassword(user, password);
		user.removeProperties(Property.KEY_PASSWORD_RECOVERY_CODE);
		modelService.update(user, operator);
		surveillanceService.audit().info("Changed password of user={}", user.getUsername());
	}

	public void setPassword(User user, String password) throws ExplodingClusterFuckException, SecurityException {
		if (!user.isNew()) {
			throw new SecurityException("Cannot set password of persistent user");
		}
		setSaltedPassword(user, password);
		surveillanceService.audit().info("Set initial password on new user");
	}

	private void setSaltedPassword(User user, String password) throws ExplodingClusterFuckException {
		String salt = passwordEncryptionService.generateSalt();
		String encryptedPassword = passwordEncryptionService.getEncryptedPassword(password, salt);
		user.setPassword(encryptedPassword);
		user.setSalt(salt);
	}

	public void changePasswordUsingKey(String key, String password, Request request) throws ExplodingClusterFuckException, SecurityException, ModelException, BadRequestException {
		User user = passwordRecoveryService.getUserByRecoveryKey(key, request);
		if (user==null) {
			throw new BadRequestException(Error.userNotFound);
		}
		Operator usersOperator = request.as(user);
		changePassword(user, password, usersOperator);
		changeUser(request, user.getUsername(), password, usersOperator);
	}

	public boolean logOut(UserSession userSession) {
		User user = getPublicUser();
		if (user==null) {
			return false;
		} else {
			surveillanceService.log(userSession, LogType.logOut);
			userSession.setUser(user, new HashSet<>());
			return true;
		}
	}

	public boolean isPublicView(Item item, Operator operator) {
		Privilege privilege = modelService.getPrivilege(item.getId(), getPublicUser().getId(), operator.getOperation());
		if (privilege!=null) {
			return privilege.isView();
		}
		return false;
	}

	public Privilege getPrivilege(long id,Privileged priviledged, Operator operator) {
		return modelService.getPrivilege(id, priviledged.getIdentity(), operator.getOperation());
	}

	public List<Privileged> expand(Privileged priviledged) {
		List<Privileged> privs = Lists.newArrayList(priviledged);
		User publicUser = getPublicUser();
		if (priviledged.getIdentity()!=publicUser.getIdentity()) {
			privs.add(publicUser);
		}
		return privs;
	}

	public boolean isOnlyPrivileged(Item item,Privileged privileged, Operator operator) {

		List<Long> ids = modelService.getPrivilegedUsers(item.getId(), operator);
		return ids.size() == 1 && ids.get(0) == privileged.getIdentity();
	}

	public boolean canView(Item item, Operator operator) {
		return canView(item, operator, operator);
	}

	public boolean canView(Item item, Privileged privileged, Operator operator) {
		if (isAdminUser(privileged)) {
			return true;
		}
		List<Privileged> expand = expand(privileged);
		for (Privileged priv : expand) {
			if (canExactlyView(item, priv, operator)) {
				return true;
			}
		}
		return false;
	}

	public boolean canDelete(Item item,Privileged privileged, Operator operator) {
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
			if (canExactlyDelete(item, priv, operator)) {
				return true;
			}
		}
		return false;
	}

	public boolean canDelete(Item item, Operator operator) {
		return canDelete(item, operator, operator);
	}

	public boolean canModify(Item item, Operator operator) {
		return canModify(item, operator, operator);
	}

	public boolean canModify(Item item, Privileged privileged, Operator operator) {
		if (isAdminUser(privileged)) {
			return true;
		}
		if (isPublicUser(privileged)) {
			return false;
		}
		List<Privileged> expand = expand(privileged);
		for (Privileged priv : expand) {
			if (canExactlyModify(item, priv, operator)) {
				return true;
			}
		}
		return false;
	}

	private boolean canExactlyView(Item item, Privileged privileged, Operator operator) {
		Privilege privilege = getPrivilege(item.getId(), privileged, operator);
		if (privilege==null) {
			return false;
		} else {
			return privilege.isView();
		}
	}

	private boolean canExactlyDelete(Item item, Privileged other, Operator operator) {
		Privilege privilege = modelService.getPrivilege(item.getId(), other.getIdentity(), operator.getOperation());
		if (privilege==null) {
			return false;
		} else {
			return privilege.isDelete();
		}
	}

	private boolean canExactlyModify(Item item, Privileged privileged, Operator operator) {
		Privilege privilege = getPrivilege(item.getId(), privileged, operator);
		if (privilege==null) {
			return false;
		} else {
			return privilege.isAlter();
		}
	}

	public User getPublicUser() {
		if (publicUser==null) {
			Operation operation = modelService.newOperation();
			publicUser = modelService.getUser(SecurityService.PUBLIC_USERNAME, operation);
			modelService.execute(operation);
		}
		return publicUser;
	}

	public boolean isPublicUser(Privileged privileged) {
		User pub = getPublicUser();
		return pub!=null && privileged.getIdentity()==pub.getIdentity();
	}

	public Privileged getAdminPrivileged() {
		if (adminPrivileged==null) {
			Operation operation = modelService.newOperation();
			User user = modelService.getUser(SecurityService.ADMIN_USERNAME, operation);
			adminPrivileged = new DummyPrivileged(user.getId());
			modelService.execute(operation);
		}
		return adminPrivileged;
	}

	public boolean isAdminUser(Privileged privileged) {
		Privileged admin = getAdminPrivileged();
		if (admin == null) {
			log.error("No admin user while checking");
		}
		return admin!=null && privileged.getIdentity() == admin.getIdentity();
	}

	public User getUserBySecret(String secret, Operator operator) {
		if (Strings.isBlank(secret)) {
			return null;
		}
		UserQuery q = new UserQuery();
		q.setSecret(secret);
		SearchResult<User> result = modelService.search(q, operator);
		return result.getFirst();
	}

	public String getSecret(ClientInfo info, User user, Operator operator) throws ModelException, SecurityException {
		String secret;
		Query<Client> q = Query.after(Client.class).withField("UUID", info.getUUID()).as(user).withPaging(0, 1);
		final Client client = modelService.getFirst(q, operator);
		if (client!=null) {
			secret = client.getPropertyValue(Property.KEY_AUTHENTICATION_SECRET);
			syncClientInfo(info, client);
			modelService.update(client, operator);
		} else {
			secret = buildSecret();
			Client newClient = new Client();
			newClient.overrideFirstProperty(Property.KEY_AUTHENTICATION_SECRET, secret);
			syncClientInfo(info, newClient);
			newClient.setUUID(info.getUUID());
			modelService.create(newClient, operator);
			modelService.createRelation(user, newClient, operator);
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

	private UserSession getInitialUser(Operator operator) {
		if (configurationService.isDevelopmentMode()) {
			String developmentUser = configurationService.getDevelopmentUser();
			if (Strings.isNotBlank(developmentUser)) {
				User user = modelService.getUser(developmentUser, operator);
				if (user!=null) {
					return new UserSession(user, getAbilities(user));
				}
			}
		}
		return new UserSession(getPublicUser());
	}

	public void makePublicVisible(Item item, Operator operator) throws SecurityException, ModelException {
		modelService.grantPrivileges(item, getPublicUser(), true, false, false, operator);
	}

	public void makePublicHidden(Item item, Operator operator) throws SecurityException {
		if (!canModify(item, operator)) {
			throw new SecurityException("The user cannot make this non public");
		}
		User publicUser = getPublicUser();
		modelService.removePrivileges(item, publicUser, operator);
	}

	public void grantPublicView(Item item, boolean view, Operator granter) throws ModelException, SecurityException {
		modelService.grantPrivileges(item, getPublicUser(), view, false, false, granter);
	}

	public void grantFullPrivileges(Item item, Privileged user, Operator granter) throws ModelException, SecurityException {
		modelService.grantPrivileges(item, user, true, true, true, granter);
	}

	private String getKey(HttpServletRequest servletRequest) {
		String auth = servletRequest.getHeader("Authorization");
		if (auth !=null) {

			Pattern pattern = Pattern.compile("Bearer (.*)");
			Matcher matcher = pattern.matcher(auth);
			if (matcher.matches()) {
				String key = matcher.group(1);
				return key;
			}
		}
		return null;
	}

	public void ensureUserSession(Request request) throws SecurityException {
		String key = getKey(request.getRequest());
		if (key!=null) {
			User user = getUserBySecret(key, request);
			if (user==null) {
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {}
				throw new SecurityException(Error.userNotFound);
			}
			request.setSession(new UserSession(user));
		}
		else {
			HttpSession session = request.getRequest().getSession(false);
			if (session == null) {
				request.setSession(getInitialUser(request));
				return;
			}
			if (session.getAttribute(UserSession.SESSION_ATTRIBUTE) == null) {
				log.trace("Creating new user session");
				session.setAttribute(UserSession.SESSION_ATTRIBUTE, getInitialUser(request));
			}
			request.setSession(UserSession.get(session));
		}
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
