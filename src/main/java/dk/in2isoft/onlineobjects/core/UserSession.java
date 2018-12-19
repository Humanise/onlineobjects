package dk.in2isoft.onlineobjects.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.apps.ApplicationController;
import dk.in2isoft.onlineobjects.apps.ApplicationSession;
import dk.in2isoft.onlineobjects.model.User;

public class UserSession implements Privileged {

	public static String SESSION_ATTRIBUTE = "OnlineObjects.UserSession";

	private Map<Class<? extends ApplicationController>, ApplicationSession> toolSessions;

	private long identity = -1;
	
	private String id;
	
	private Set<Ability> abilities;

	private String username;

	public UserSession(User user) {
		this.id = Strings.generateRandomString(50);
		toolSessions = new HashMap<Class<? extends ApplicationController>, ApplicationSession>();
		changeUser(user, new HashSet<Ability>());
	}
	
	public String getId() {
		return id;
	}
	
	public static UserSession get(HttpSession session) {
		Object object = session.getAttribute(SESSION_ATTRIBUTE);
		if (object instanceof UserSession) {
			return (UserSession) object;
		}
		return null;
	}
	
	protected void setUser(User user, Set<Ability> abilities) {
		if (user==null) {
			throw new IllegalArgumentException("Cannot set the user to null");
		}
		if (user.isNew()) {
			throw new IllegalArgumentException("Cannot set a user that is not persistent");
		}
		changeUser(user, abilities);
	}

	private void changeUser(User user, Set<Ability> abilities) {
		this.username = user.getUsername();
		this.identity = user.getIdentity();
		this.abilities = abilities;
	}

	@Deprecated
	public String getUsername() {
		return username;
	}
	
	public boolean has(Ability ability) {
		return abilities.contains(ability);
	}
	
	public long getIdentity() {
		return identity;
	}

	public ApplicationSession getApplicationSession(ApplicationController controller) {
		if (this.toolSessions.containsKey(controller.getClass())) {
			return toolSessions.get(controller.getClass());
		} else {
			ApplicationSession session = controller.createToolSession();
			if (session != null) {
				toolSessions.put(controller.getClass(), session);
			}
			return session;
		}
	}

	@Override
	public String toString() {
		return "User session for user:" + username;
	}
	
	
}