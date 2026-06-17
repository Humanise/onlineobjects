package org.onlineobjects.modules.intelligence;

public class EmbeddingInfo {

	private EmbeddingModel model;
	private float[] vector;
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

	public float[] getVector() {
		return vector;
	}

	public static EmbeddingInfo create(float[] vector, EmbeddingModel model) {
		EmbeddingInfo e = new EmbeddingInfo();
		e.vector = vector;
		e.model = model;
		return e;
	}
}
