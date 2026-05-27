package org.onlineobjects.modules.intelligence;

import java.time.Duration;

public enum EmbeddingModel {

	GEMINI_1(1, 3072, 10),
	GEMINI_1_SMALL(1, 768, 10),
	GEMINI_2_PREVIEW(2, 3072, 10),
	GEMINI_2_PREVIEW_SMALL(2, 768, 10),
	OLLAMA_GEMINI(3, 768, 0)
	;

	private int id;
	private int dimensions;
	private long delay;

	EmbeddingModel(int id, int dimensions, long delay) {
		this.id = id;
		this.dimensions = dimensions;
		this.delay = delay;
	}

	public int getId() {
		return id;
	}

	public int getDimensions() {
		return dimensions;
	}

	public Duration getRequestDelay() {
		return Duration.ofMillis(delay);
	}
}
