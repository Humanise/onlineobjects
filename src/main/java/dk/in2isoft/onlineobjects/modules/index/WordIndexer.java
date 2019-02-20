package dk.in2isoft.onlineobjects.modules.index;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.events.ModelEventListener;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.Word;
import dk.in2isoft.onlineobjects.modules.language.WordListPerspective;

public class WordIndexer implements ModelEventListener, Indexer {
		
	private WordIndexDocumentBuilder documentBuilder;
	private ModelService modelService;
	
	private IndexManager indexManager;
	
	private static final Logger log = LogManager.getLogger(WordIndexer.class);
	
	private boolean enabled = true;

	public void clear() throws EndUserException {
		indexManager.clear();
	}
	
	@Override
	public List<IndexDescription> getIndexInstances(Operator operator) {
		return Lists.newArrayList(new IndexDescription(indexManager.getDirectoryName()));
	}
	
	@Override
	public boolean is(IndexDescription description) {
		return description.getName().equals(indexManager.getDirectoryName());
	}
	
	@Override
	public long getObjectCount(IndexDescription description, Operator operator) {
		return modelService.count(Query.after(Word.class), operator);
	}
	
	public void indexWord(Word word) {
		if (!enabled) {
			return;
		}
		try {
			Document document = documentBuilder.build(word, null);
			log.debug("Re-indexing : "+word);
			indexManager.update(word, document);
		} catch (EndUserException e) {
			log.error("Unable to reindex: "+word, e);
		}
	}

	public void indexWordPerspectives(List<WordListPerspective> words) {
		if (!enabled) {
			return;
		}
		try {
			Map<Entity,Document> map = Maps.newHashMap();
			for (WordListPerspective perspective : words) {
				Document document = documentBuilder.build(perspective);
				log.debug("Re-indexing : "+perspective);
				Word word = new Word();
				word.setText(perspective.getText());
				word.setName(perspective.getText());
				word.setId(perspective.getId());
				map.put(word, document);
			}
			indexManager.update(map);
		} catch (EndUserException e) {
			log.error("Unable to reindex", e);
		}			
	}

	public void indexWords(List<Word> words) {
		if (!enabled) {
			return;
		}
		try {
			Map<Entity,Document> map = Maps.newHashMap();
			for (Word word : words) {
				Document document = documentBuilder.build(word, null);
				log.debug("Re-indexing : "+word);
				map.put(word, document);
			}
			indexManager.update(map);
		} catch (EndUserException e) {
			log.error("Unable to reindex", e);
		}			
	}
	
	// Listeners
	
	public void entityWasCreated(Entity entity) {
		if (entity instanceof Word) {
			indexWord((Word) entity);
		}
	}

	public void entityWasUpdated(Entity entity) {
		if (entity instanceof Word) {
			indexWord((Word) entity);
		}		
	}

	public void entityWasDeleted(Entity entity) {
		if (entity instanceof Word) {
			try {
				indexManager.delete(entity.getId());
			} catch (EndUserException e) {
				log.error("Unable to remove: "+entity, e);
			}
		}
	}

	public void relationWasCreated(Relation relation) {
		if (relation.getFrom() instanceof Word) {
			indexWord((Word) relation.getFrom());
		}
		if (relation.getTo() instanceof Word) {
			indexWord((Word) relation.getTo());
		}
	}

	public void relationWasUpdated(Relation relation) {
		if (relation.getFrom() instanceof Word) {
			indexWord((Word) relation.getFrom());
		}
		if (relation.getTo() instanceof Word) {
			indexWord((Word) relation.getTo());
		}
	}

	public void relationWasDeleted(Relation relation) {
		if (relation.getFrom() instanceof Word) {
			indexWord((Word) relation.getFrom());
		}
		if (relation.getTo() instanceof Word) {
			indexWord((Word) relation.getTo());
		}
	}

	// Settings
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	// Wiring...
	
	public void setIndexManager(IndexManager indexManager) {
		this.indexManager = indexManager;
	}
	
	public void setDocumentBuilder(WordIndexDocumentBuilder documentBuilder) {
		this.documentBuilder = documentBuilder;
	}
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
}
