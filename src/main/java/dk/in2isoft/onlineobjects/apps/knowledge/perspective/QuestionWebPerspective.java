package dk.in2isoft.onlineobjects.apps.knowledge.perspective;

import java.util.List;

import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.ui.data.Option;

public class QuestionWebPerspective implements CategorizableViewPerspective, ViewPerspectiveWithTags, TaggableViewPerspective {

	private long id;
	private String text;
	private List<KnowledgeWebPerspective> answers;
	private boolean inbox;
	private boolean favorite;
	private String type = Question.class.getSimpleName();
	private List<Option> words;
	private List<Option> tags;

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

	public List<KnowledgeWebPerspective> getAnswers() {
		return answers;
	}

	public void setAnswers(List<KnowledgeWebPerspective> answers) {
		this.answers = answers;
	}

	public List<Option> getWords() {
		return words;
	}

	public void setWords(List<Option> words) {
		this.words = words;
	}

	public List<Option> getTags() {
		return tags;
	}

	@Override
	public void setTags(List<Option> tags) {
		this.tags = tags;
	}

	public static QuestionWebPerspective from(Question question) {
		QuestionWebPerspective perspective = new QuestionWebPerspective();
		perspective.setId(question.getId());
		perspective.setText(question.getText());
		return perspective;
	}
}
