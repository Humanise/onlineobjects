package dk.in2isoft.onlineobjects.modules.knowledge;

import java.util.List;

import dk.in2isoft.onlineobjects.apps.knowledge.perspective.CategorizableViewPerspective;

public class StatementApiPerspective implements CategorizableViewPerspective, ApiPerspective {

	private String text;
	private long id;
	private Boolean inbox;
	private Boolean favorite;
	private List<PersonApiPerspective> authors;
	private List<InternetAddressApiPerspective> addresses;
	private List<QuestionApiPerspective> questions;
	private long version;

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

	public long getVersion() {
		return version;
	}
	
	public void setVersion(long version) {
		this.version = version;
	}
	
	public Boolean isInbox() {
		return inbox;
	}

	public void setInbox(Boolean inbox) {
		this.inbox = inbox;
	}

	public Boolean isFavorite() {
		return favorite;
	}

	public void setFavorite(Boolean favorite) {
		this.favorite = favorite;
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

	public List<QuestionApiPerspective> getQuestions() {
		return questions;
	}

	public void setQuestions(List<QuestionApiPerspective> questions) {
		this.questions = questions;
	}
}
