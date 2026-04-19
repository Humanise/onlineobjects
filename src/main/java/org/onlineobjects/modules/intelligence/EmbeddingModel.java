package org.onlineobjects.modules.intelligence;

public enum EmbeddingModel {

	GEMINI_1(1, 3072),
	GEMINI_1_SMALL(1, 768),
	GEMINI_2_PREVIEW(2, 3072),
	GEMINI_2_PREVIEW_SMALL(2, 768),
	OLLAMA_GEMINI(3, 768)
	;

	private int id;
	private int dimensions;

	EmbeddingModel(int id, int dimensions) {
		this.id = id;
		this.dimensions = dimensions;
	}

	public int getId() {
		return id;
	}

	public int getDimensions() {
		return dimensions;
	}
}
