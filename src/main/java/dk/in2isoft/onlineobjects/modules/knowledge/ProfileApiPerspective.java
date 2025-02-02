package dk.in2isoft.onlineobjects.modules.knowledge;

public class ProfileApiPerspective implements ApiPerspective {
	private long version;
	private String username;
	private String fullName;
	private String email;
	private long profilePhotoId;

	public long getVersion() {
		return version;
	}
	
	public void setVersion(long version) {
		this.version = version;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public long getProfilePhotoId() {
		return profilePhotoId;
	}

	public void setProfilePhotoId(long profilePhotoId) {
		this.profilePhotoId = profilePhotoId;
	}
}
