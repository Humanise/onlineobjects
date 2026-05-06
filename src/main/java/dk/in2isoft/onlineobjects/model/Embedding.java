package dk.in2isoft.onlineobjects.model;

import java.util.List;

public class Embedding {

	private Long id;
	private Long modelId;
	private Long entityId;
	private List<Double> embedding;
	private String text;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getModelId() {
		return modelId;
	}

	public void setModelId(Long modelId) {
		this.modelId = modelId;
	}

	public Long getEntityId() {
		return entityId;
	}

	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}

	public List<Double> getEmbedding() {
		return embedding;
	}

	public void setEmbedding(List<Double> embedding) {
		this.embedding = embedding;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
