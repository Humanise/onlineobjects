package dk.in2isoft.onlineobjects.model;

import dk.in2isoft.onlineobjects.core.Ability;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.model.annotations.Appearance;

@Appearance(icon="common/user")
public class User extends Entity implements Privileged {

	public static String TYPE = Entity.TYPE+"/User";
	public static String NAMESPACE = Entity.NAMESPACE+"User/";
	public static final String FIELD_USERNAME = "username";
	
	private String username;
	private String password;
	private String salt;

	public User() {
		super();
	}

	public String getType() {
		return TYPE;
	}

	public String getIcon() {
		return "common/user";
	}

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		if (name==null || name.equals(this.username)) {
			this.name = username;			
		}
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public long getIdentity() {
		return getId();
	}

	public boolean hasAbility(Ability ability) {
		if (ability == null) return false;
		return hasProperty(Property.KEY_ABILITY, ability.name());
	}
}
