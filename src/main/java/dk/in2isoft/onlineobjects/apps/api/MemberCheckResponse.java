package dk.in2isoft.onlineobjects.apps.api;

public class MemberCheckResponse {

	private Boolean emailValid;
	private Boolean emailTaken;

	private Boolean usernameValid;
	private Boolean usernameTaken;

	private Boolean passwordValid;

	public Boolean getEmailValid() {
		return emailValid;
	}

	public void setEmailValid(Boolean emailValid) {
		this.emailValid = emailValid;
	}

	public Boolean getEmailTaken() {
		return emailTaken;
	}

	public void setEmailTaken(Boolean emailTaken) {
		this.emailTaken = emailTaken;
	}

	public Boolean getUsernameValid() {
		return usernameValid;
	}

	public void setUsernameValid(Boolean usernameValid) {
		this.usernameValid = usernameValid;
	}

	public Boolean getUsernameTaken() {
		return usernameTaken;
	}

	public void setUsernameTaken(Boolean usernameTaken) {
		this.usernameTaken = usernameTaken;
	}

	public Boolean getPasswordValid() {
		return passwordValid;
	}

	public void setPasswordValid(Boolean passwordValid) {
		this.passwordValid = passwordValid;
	}
}
