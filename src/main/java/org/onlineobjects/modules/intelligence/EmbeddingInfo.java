package org.onlineobjects.modules.intelligence;

import java.util.List;

public class EmbeddingInfo {

	private EmbeddingModel model;
	private List<Double> vector;
	private String text;

	public EmbeddingModel getModel() {
		return model;
	}

	public void setModel(EmbeddingModel model) {
		this.model = model;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public List<Double> getVector() {
		return vector;
	}

	public static EmbeddingInfo create(List<Double> vector, EmbeddingModel model) {
		EmbeddingInfo e = new EmbeddingInfo();
		e.vector = vector;
		e.model = model;
		return e;
	}
}
