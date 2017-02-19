package dk.in2isoft.onlineobjects.apps.setup.perspectives;

public class UserPerspective {

	private long id;
	private String name;
	private String username;
	private boolean publicView;

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
}
