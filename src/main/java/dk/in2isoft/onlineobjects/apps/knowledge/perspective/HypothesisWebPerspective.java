package dk.in2isoft.onlineobjects.apps.knowledge.perspective;

import java.util.List;

import dk.in2isoft.onlineobjects.model.Hypothesis;
import dk.in2isoft.onlineobjects.ui.data.Option;

public class HypothesisWebPerspective implements CategorizableViewPerspective, ViewPerspectiveWithTags, TaggableViewPerspective {

	private long id;
	private String type = Hypothesis.class.getSimpleName();
	private String text;
	private boolean inbox;
	private boolean favorite;
	private List<StatementWebPerspective> supports;
	private List<StatementWebPerspective> contradicts;
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

	public List<StatementWebPerspective> getSupports() {
		return supports;
	}

	public void setSupports(List<StatementWebPerspective> supporting) {
		this.supports = supporting;
	}

	public List<StatementWebPerspective> getContradicts() {
		return contradicts;
	}

	public void setContradicts(List<StatementWebPerspective> contradicting) {
		this.contradicts = contradicting;
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
}
