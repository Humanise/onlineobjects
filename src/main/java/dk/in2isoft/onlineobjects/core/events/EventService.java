package dk.in2isoft.onlineobjects.core.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.event.spi.PostCommitDeleteEventListener;
import org.hibernate.event.spi.PostCommitInsertEventListener;
import org.hibernate.event.spi.PostCommitUpdateEventListener;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;

import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Item;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.User;

public class EventService implements PostCommitUpdateEventListener, PostCommitInsertEventListener, PostCommitDeleteEventListener {

	private static final long serialVersionUID = 1L;

	private List<ModelEventListener> modelEventListeners = new CopyOnWriteArrayList<ModelEventListener>();
	private static Logger log = LogManager.getLogger(EventService.class);
	
	public void setModelEventListeners(List<ModelEventListener> modelEventListeners) {
		this.modelEventListeners.addAll(modelEventListeners);
	}
	
	public void addModelEventListener(ModelEventListener listener) {
		this.modelEventListeners.add(listener);
	}
	
	public void fireItemWasCreated(Item item) {
		log.info("Item was created: "+item);
		for (ModelEventListener listener : modelEventListeners) {
			if (item instanceof Entity) {
				listener.entityWasCreated((Entity) item);
			}
			if (item instanceof Relation) {
				listener.relationWasCreated((Relation) item);
			}
		}
	}
	
	public void fireItemWasUpdated(Item item) {
		log.info("Item was updated: "+item);
		for (ModelEventListener listener : modelEventListeners) {
			if (item instanceof Entity) {
				listener.entityWasUpdated((Entity) item);
			}
			if (item instanceof Relation) {
				listener.relationWasUpdated((Relation) item);
			}
		}
	}

	public void fireItemWasDeleted(Item item) {
		log.info("Item was deleted: "+item);
		for (ModelEventListener listener : modelEventListeners) {
			if (item instanceof Entity) {
				listener.entityWasDeleted((Entity) item);
			}
			if (item instanceof Relation) {
				listener.relationWasDeleted((Relation) item);
			}
		}
	}

	public void firePrivilegesRemoved(Item item, List<User> users) {
		log.info("Privileges was removed: "+item);
		for (ModelEventListener listener : modelEventListeners) {
			if (listener instanceof ModelPrivilegesEventListener) {
				((ModelPrivilegesEventListener) listener).allPrivilegesWasRemoved(item, users);
			}
		}
	}

	@Override
	public boolean requiresPostCommitHanding(EntityPersister persister) {
		return true;
	}

	@Override
	public void onPostUpdate(PostUpdateEvent event) {
		log.debug("onPostUpdate: {}" ,event.getEntity());
	}

	@Override
	public void onPostUpdateCommitFailed(PostUpdateEvent event) {
		
	}

	@Override
	public void onPostInsert(PostInsertEvent event) {
		log.debug("onPostInsert: {}" ,event.getEntity());
	}

	@Override
	public void onPostDelete(PostDeleteEvent event) {
		log.debug("onPostDelete: {}" ,event.getEntity());
	}

	@Override
	public void onPostInsertCommitFailed(PostInsertEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPostDeleteCommitFailed(PostDeleteEvent event) {
		// TODO Auto-generated method stub
		
	}
}
