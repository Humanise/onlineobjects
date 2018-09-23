package dk.in2isoft.onlineobjects.modules.index;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import dk.in2isoft.commons.lang.Code;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.events.ModelEventListener;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Relation;

public class ConfigurableIndexer<E extends Entity> implements ModelEventListener, Indexer {

	private Class<E> type;
	
	private IndexManager indexManager;
	private ModelService modelService;

	private IndexDocumentBuilder<E> documentBuilder;
	
	private static final Logger log = LogManager.getLogger(ConfigurableIndexer.class);

	public Class<E> getType() {
		return type;
	}
	
	@Override
	public List<IndexDescription> getIndexInstances() {
		return Lists.newArrayList(new IndexDescription(indexManager.getDirectoryName()));
	}

	@Override
	public boolean is(IndexDescription description) {
		// TODO Auto-generated method stub
		return description.getName().equals(indexManager.getDirectoryName());
	}
	
	@Override
	public long getObjectCount(IndexDescription description) {
		return modelService.count(Query.after(type));
	}
	
	public void clear() throws EndUserException {
		indexManager.clear();
	}
	
	public void entityWasCreated(Entity entity) {
		if (type.isAssignableFrom(entity.getClass())) {
			index(entity);
		}
	}
	
	public void index(Entity entity) {
		try {
			E ent = Code.cast(entity);
			Document document = documentBuilder.build(ent);
			log.debug("Re-indexing : "+entity);
			indexManager.update(entity, document);
		} catch (EndUserException e) {
			log.error("Unable to reindex: "+entity, e);
		}
	}

	public void index(List<E> words) {
		try {
			Map<Entity,Document> map = Maps.newHashMap();
			for (E word : words) {
				Document document = documentBuilder.build(word);
				log.debug("Re-indexing : "+word);
				map.put(word, document);
			}
			indexManager.update(map);
		} catch (EndUserException e) {
			log.error("Unable to reindex", e);
		}			
	}

	public void entityWasUpdated(Entity entity) {
		if (type.isAssignableFrom(entity.getClass())) {
			index(entity);
		}		
	}

	public void entityWasDeleted(Entity entity) {
		if (type.isAssignableFrom(entity.getClass())) {
			try {
				indexManager.delete(entity.getId());
			} catch (EndUserException e) {
				log.error("Unable to remove: "+entity, e);
			}
		}
	}

	public void relationWasCreated(Relation relation) {
		if (type.isAssignableFrom(relation.getFrom().getClass())) {
			index(relation.getFrom());
		}
		if (type.isAssignableFrom(relation.getTo().getClass())) {
			index(relation.getTo());
		}
	}

	public void relationWasUpdated(Relation relation) {
		if (type.isAssignableFrom(relation.getFrom().getClass())) {
			index(relation.getFrom());
		}
		if (type.isAssignableFrom(relation.getTo().getClass())) {
			index(relation.getTo());
		}
	}

	public void relationWasDeleted(Relation relation) {
		if (type.isAssignableFrom(relation.getFrom().getClass())) {
			index(relation.getFrom());
		}
		if (type.isAssignableFrom(relation.getTo().getClass())) {
			index(relation.getTo());
		}
	}

	// Wiring...
	
	public void setIndexManager(IndexManager indexManager) {
		this.indexManager = indexManager;
	}
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
	public void setDocumentBuilder(IndexDocumentBuilder<E> documentBuilder) {
		this.documentBuilder = documentBuilder;
	}
	
	public void setType(Class<E> type) {
		this.type = type;
	}
}
