package dk.in2isoft.onlineobjects.modules.knowledge;

import java.util.List;

public class StatementApiPerspective {

	private String text;
	private long id;
	private List<PersonApiPerspective> authors;
	private List<InternetAddressApiPerspective> addresses;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public List<PersonApiPerspective> getAuthors() {
		return authors;
	}

	public void setAuthors(List<PersonApiPerspective> authors) {
		this.authors = authors;
	}

	public List<InternetAddressApiPerspective> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<InternetAddressApiPerspective> addresses) {
		this.addresses = addresses;
	}
}
