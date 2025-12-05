package dk.in2isoft.onlineobjects.modules.knowledge;

import java.util.List;

import dk.in2isoft.onlineobjects.apps.knowledge.perspective.CategorizableViewPerspective;

public class HypothesisApiPerspective implements CategorizableViewPerspective, ApiPerspective {

	private long id;
	private long version;
	private String text;
	private Boolean inbox;
	private Boolean favorite;

	private List<StatementApiPerspective> supporting;
	private List<StatementApiPerspective> contradicting;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
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
