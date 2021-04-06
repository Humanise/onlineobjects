package dk.in2isoft.onlineobjects.apps.knowledge.perspective;

import java.util.List;

import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.ui.data.Option;

public class QuestionWebPerspective implements CategorizableViewPerspective, ViewPerspectiveWithTags {

	private long id;
	private String text;
	private List<StatementWebPerspective> answers;
	private boolean inbox;
	private boolean favorite;
	private String type = Question.class.getSimpleName();
	private List<Option> words;

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

	public List<StatementWebPerspective> getAnswers() {
		return answers;
	}

	public void setAnswers(List<StatementWebPerspective> answers) {
		this.answers = answers;
	}

	public List<Option> getWords() {
		return words;
	}

	public void setWords(List<Option> words) {
		this.words = words;
	}
}
