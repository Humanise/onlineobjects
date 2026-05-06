package org.onlineobjects.modules.intelligence;

public interface Embedder {

	boolean accepts(EmbeddingModel model);

	EmbeddingInfo embed(String text, EmbeddingModel model);

}