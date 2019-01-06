package dk.in2isoft.onlineobjects.modules.knowledge;

import java.util.List;

import dk.in2isoft.onlineobjects.apps.knowledge.perspective.CategorizableViewPerspective;

public class HypothesisApiPerspective implements CategorizableViewPerspective {

	private String text;
	private long id;
	private boolean inbox;
	private boolean favorite;

	private List<StatementApiPerspective> supporting;
	private List<StatementApiPerspective> contradicting;

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

	public List<StatementApiPerspective> getSupporting() {
		return supporting;
	}

	public void setSupporting(List<StatementApiPerspective> supporting) {
		this.supporting = supporting;
	}

	public List<StatementApiPerspective> getContradicting() {
		return contradicting;
	}

	public void setContradicting(List<StatementApiPerspective> contradicting) {
		this.contradicting = contradicting;
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
	
}
