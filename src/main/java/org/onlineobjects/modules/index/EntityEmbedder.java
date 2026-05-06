package org.onlineobjects.modules.index;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.onlineobjects.modules.intelligence.EmbeddingInfo;
import org.onlineobjects.modules.intelligence.EmbeddingModel;
import org.onlineobjects.modules.intelligence.Intelligence;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Embedding;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.TextHolding;

public class EntityEmbedder {

	private ModelService model;
	private Intelligence intelligence;
	private static Logger log = LogManager.getLogger(EntityEmbedder.class);


	public void embed(Entity entity) throws ModelException, SecurityException {
		EmbeddingModel embeddingModel = intelligence.getSearchEmbeddingModel().orElse(null);
		if (embeddingModel == null) return;
		try (Operator operator = model.newAdminOperator()) {
			Query<Embedding> query = Query.after(Embedding.class).withField("entityId", entity.getId()).withField("modelId", embeddingModel.getId());
			Embedding embedding = model.search(query, operator).getFirst();
			if (embedding == null) {
				embedding = new Embedding();

				String text = asString(entity);
				EmbeddingInfo embeddingInfo = intelligence.embed(text, embeddingModel).orElse(null);
				if (embeddingInfo == null) {
					log.error("Unable to get embedding");
					return;
				}
				embedding.setEmbedding(embeddingInfo.getVector());
				embedding.setEntityId(entity.getId());
				embedding.setModelId((long) embeddingModel.getId());
				embedding.setText(text);
				model.create(embedding, operator);
			} else {
				String text = asString(entity);

				if (!Objects.equals(text, embedding.getText())) {
					EmbeddingInfo embeddingInfo = intelligence.embed(text, embeddingModel).orElse(null);
					if (embeddingInfo == null) {
						log.error("Unable to get embedding");
						return;
					}
					embedding.setEmbedding(embeddingInfo.getVector());
					embedding.setText(text);
					model.update(embedding, operator);
				}
			}
		}
	}

	private String asString(Entity entity) {
		if (entity instanceof TextHolding) {
			return ((TextHolding) entity).getText();
		}
		return entity.getName();
	}

	@Autowired
	public void setModel(ModelService model) {
		this.model = model;
	}

	@Autowired
	public void setIntelligence(Intelligence intelligence) {
		this.intelligence = intelligence;
	}
}
