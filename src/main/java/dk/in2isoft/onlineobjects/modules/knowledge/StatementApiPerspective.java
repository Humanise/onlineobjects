package dk.in2isoft.onlineobjects.modules.knowledge;

import java.util.List;

import dk.in2isoft.onlineobjects.apps.knowledge.perspective.CategorizableViewPerspective;

public class StatementApiPerspective implements CategorizableViewPerspective {

	private String text;
	private long id;
	private boolean inbox;
	private boolean favorite;
	private List<PersonApiPerspective> authors;
	private List<InternetAddressApiPerspective> addresses;
	private List<QuestionApiPerspective> questions;

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

	public boolean isInbox() {
		return inbox;
	}

	public void setInbox(boolean inbox) {
		this.inbox = inbox;
	}

	public boolean isFavorite() {
		return favorite;
	}

	public void setFavorite(boolean favorite) {
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
