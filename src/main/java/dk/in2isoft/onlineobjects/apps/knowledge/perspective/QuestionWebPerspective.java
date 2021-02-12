package dk.in2isoft.onlineobjects.apps.knowledge.perspective;

import java.util.List;

import dk.in2isoft.onlineobjects.model.Question;

public class QuestionWebPerspective implements CategorizableViewPerspective {

	private long id;
	private String text;
	private List<QuotePerspective> answers;
	private boolean inbox;
	private boolean favorite;
	private String type = Question.class.getSimpleName();

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public String getType() {
		return type;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
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

	public List<QuotePerspective> getAnswers() {
		return answers;
	}

	public void setAnswers(List<QuotePerspective> answers) {
		this.answers = answers;
	}
}
