package dk.in2isoft.onlineobjects.modules.knowledge;

import java.util.List;

import dk.in2isoft.onlineobjects.apps.knowledge.perspective.CategorizableViewPerspective;

public class QuestionApiPerspective implements CategorizableViewPerspective, ApiPerspective {

	private long id;
	private long version;
	private String text;
	private Boolean inbox;
	private Boolean favorite;

	private List<StatementApiPerspective> answers;


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
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public List<StatementApiPerspective> getAnswers() {
		return answers;
	}
	
	public void setAnswers(List<StatementApiPerspective> answers) {
		this.answers = answers;
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
}
