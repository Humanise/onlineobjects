package dk.in2isoft.onlineobjects.modules.knowledge;

import java.util.List;

public class HypothesisApiPerspective {

	private String text;
	private long id;

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
	
}
