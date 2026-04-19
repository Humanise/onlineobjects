package org.onlineobjects.modules.index;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.onlineobjects.core.events.ModelEventListener;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Hypothesis;
import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.Statement;

public class EntityEmbeddingListener implements ModelEventListener {

	private EntityEmbedder embedder;
	private static Logger log = LogManager.getLogger(EntityEmbeddingListener.class);

	@Override
	public void entityWasCreated(Entity entity) {
		update(entity);
	}

	private boolean isSupported(Entity entity) {
		return (entity instanceof Question || entity instanceof Statement || entity instanceof Hypothesis);
	}

	private void update(Entity entity) {
		if (!isSupported(entity)) return;
		try {
			embedder.embed(entity);
		} catch (ModelException | SecurityException e) {
			log.error(e);
		}
	}

	@Override
	public void entityWasUpdated(Entity entity) {
		update(entity);
	}

	@Override
	public void entityWasDeleted(Entity entity) {

	}

	@Override
	public void relationWasCreated(Relation relation) {

	}

	@Override
	public void relationWasUpdated(Relation relation) {

	}

	@Override
	public void relationWasDeleted(Relation relation) {

	}

	@Autowired
	public void setEmbedder(EntityEmbedder embedder) {
		this.embedder = embedder;
	}
}
