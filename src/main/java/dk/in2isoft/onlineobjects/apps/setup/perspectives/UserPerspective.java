package dk.in2isoft.onlineobjects.apps.setup.perspectives;

import java.util.Set;

import dk.in2isoft.onlineobjects.core.Ability;

public class UserPerspective {

	private long id;
	private String name;
	private String username;
	private String email;
	private boolean publicView;
	private Set<Ability> abilities;

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setPublicView(boolean publicView) {
		this.publicView = publicView;
	}

	public boolean isPublicView() {
		return publicView;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Set<Ability> getAbilities() {
		return abilities;
	}

	public void setAbilities(Set<Ability> abilities) {
		this.abilities = abilities;
	}
}
