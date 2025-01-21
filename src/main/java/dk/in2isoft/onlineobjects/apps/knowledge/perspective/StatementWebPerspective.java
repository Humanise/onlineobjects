package dk.in2isoft.onlineobjects.apps.knowledge.perspective;

import java.util.List;

import org.onlineobjects.modules.suggestion.SuggestionsCategory;

import dk.in2isoft.onlineobjects.model.Statement;
import dk.in2isoft.onlineobjects.ui.data.Option;

public class StatementWebPerspective implements KnowledgeWebPerspective, CategorizableViewPerspective, ViewPerspectiveWithTags, TaggableViewPerspective {

	private long id;
	private String text;
	private boolean inbox;
	private boolean favorite;
	private List<QuestionWebPerspective> questions;
	private List<InternetAddressViewPerspective> addresses;
	private List<HypothesisWebPerspective> supports;
	private List<HypothesisWebPerspective> contradicts;
	private SuggestionsCategory questionSuggestions;
	private String type = Statement.class.getSimpleName();
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

	public List<QuestionWebPerspective> getQuestions() {
		return questions;
	}

	public void setQuestions(List<QuestionWebPerspective> questions) {
		this.questions = questions;
	}

	public List<InternetAddressViewPerspective> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<InternetAddressViewPerspective> addresses) {
		this.addresses = addresses;
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
	
	public void setTags(List<Option> tags) {
		this.tags = tags;
	}

	public SuggestionsCategory getQuestionSuggestions() {
		return questionSuggestions;
	}

	public void setQuestionSuggestions(SuggestionsCategory questionSuggestions) {
		this.questionSuggestions = questionSuggestions;
	}

	public static StatementWebPerspective from(Statement answer) {
		var p = new StatementWebPerspective();
		p.setId(answer.getId());
		p.setText(answer.getText());
		return p;
	}

	public List<HypothesisWebPerspective> getSupports() {
		return supports;
	}

	public void setSupports(List<HypothesisWebPerspective> supports) {
		this.supports = supports;
	}

	public List<HypothesisWebPerspective> getContradicts() {
		return contradicts;
	}

	public void setContradicts(List<HypothesisWebPerspective> contradicts) {
		this.contradicts = contradicts;
	}
}
