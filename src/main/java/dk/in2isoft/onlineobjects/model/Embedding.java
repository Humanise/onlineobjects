package dk.in2isoft.onlineobjects.model;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

public class Embedding {

	private Long id;
	private Long modelId;
	private Long entityId;
	@JdbcTypeCode(SqlTypes.VECTOR)
	private float[] embedding;
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

	public float[] getEmbedding() {
		return embedding;
	}

	public void setEmbedding(float[] embedding) {
		this.embedding = embedding;
	}

	public void setEmbedding(double[] doubles) {
		this.embedding = new float[doubles.length];
		for (int i = 0; i < doubles.length; i++) {
			this.embedding[i] = (float) doubles[i];
		}
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
