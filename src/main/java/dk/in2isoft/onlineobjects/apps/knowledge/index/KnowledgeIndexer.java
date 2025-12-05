package dk.in2isoft.onlineobjects.apps.knowledge.index;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;

import dk.in2isoft.onlineobjects.core.DummyPrivileged;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.Results;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.events.ModelEventListener;
import dk.in2isoft.onlineobjects.core.events.ModelPrivilegesEventListener;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Hypothesis;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.model.Item;
import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.Statement;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.index.IndexDescription;
import dk.in2isoft.onlineobjects.modules.index.IndexManager;
import dk.in2isoft.onlineobjects.modules.index.IndexService;
import dk.in2isoft.onlineobjects.modules.index.Indexer;

public class KnowledgeIndexer implements ModelEventListener, ModelPrivilegesEventListener, Indexer {

	private static final String APP_READER_USER  = "app-reader-user-";

	private KnowledgeIndexDocumentBuilder documentBuilder;

	private IndexService indexService;
	private ModelService modelService;
	private SecurityService securityService;
	private KnowledgeSolrIndexWriter knowledgeSolrIndexWriter;

	private static final Logger log = LogManager.getLogger(KnowledgeIndexer.class);

	public void clear(Privileged privileged) throws EndUserException {
		getIndexManager(privileged).clear();
	}

	@Override
	public boolean is(IndexDescription description) {
		return description.getName().startsWith(APP_READER_USER );
	}

	@Override
	public long getObjectCount(IndexDescription description, Operator operator) {
		DummyPrivileged privileged = new DummyPrivileged(description.getUserId());
		long count = 0;
		Class<?>[] types = {InternetAddress.class, Question.class, Statement.class, Hypothesis.class};
		for (Class<?> type : types) {
			count += modelService.count(Query.after(type).as(privileged), operator);
		}
		return count;
	}

	@Override
	public List<IndexDescription> getIndexInstances(Operator operator) {
		Query<User> query = Query.after(User.class);
		return modelService.search(query, operator).getList().stream().map(user -> {
			IndexDescription desc = new IndexDescription();
			desc.setName(getIndexName(user));
			desc.setUserId(user.getId());
			return desc;
		}).collect(Collectors.toList());
	}

	public void reIndex(Operator operator) throws EndUserException {

		clear(operator);
		{
			Query<InternetAddress> query = Query.after(InternetAddress.class).as(operator);
			Results<InternetAddress> scroll = modelService.scroll(query, operator);
			try {
				while (scroll.next()) {
					InternetAddress address = scroll.get();
					index(address);
				}
			} finally {
				scroll.close();
			}
		}
		{
			Query<Statement> query = Query.after(Statement.class).as(operator);
			Results<Statement> scroll = modelService.scroll(query, operator);
			try {
				while (scroll.next()) {
					Statement statement = scroll.get();
					index(statement);
				}
			} finally {
				scroll.close();
			}
		}
		{
			Query<Question> query = Query.after(Question.class).as(operator);
			Results<Question> scroll = modelService.scroll(query, operator);
			try {
				while (scroll.next()) {
					Question question = scroll.get();
					index(question);
				}
			} finally {
				scroll.close();
			}
		}
		{
			Query<Hypothesis> query = Query.after(Hypothesis.class).as(operator);
			Results<Hypothesis> scroll = modelService.scroll(query, operator);
			try {
				while (scroll.next()) {
					Hypothesis question = scroll.get();
					index(question);
				}
			} finally {
				scroll.close();
			}
		}
	}

	public void index(InternetAddress address) {
		Privileged privileged = securityService.getAdminPrivileged();
		Operator operator = modelService.newOperator(privileged);
		try {
			address = modelService.get(InternetAddress.class, address.getId(), operator);
			if (address != null) {
				User owner = modelService.getOwner(address, operator);
				if (owner!=null) {
					Document document = documentBuilder.build(address, operator, owner);
					log.debug("Re-indexing : "+address);
					index(address, owner, document);
				}
			}
			operator.commit();
		} catch (EndUserException e) {
			log.error("Unable to reindex: "+address, e);
			operator.rollBack();
		}
	}

	private void index(Entity entity, User owner, Document document) throws EndUserException {
		getIndexManager(owner).update(entity, document);
		knowledgeSolrIndexWriter.index(entity, owner, document);
	}

	public void index(Question question) {
		Privileged privileged = securityService.getAdminPrivileged();
		Operator operator = modelService.newOperator(privileged);
		try {
			User owner = modelService.getOwner(question, operator);
			if (owner!=null) {
				Document doc = documentBuilder.build(question, operator, owner);

				index(question, owner, doc);
			}
			operator.commit();
		} catch (EndUserException e) {
			log.error("Unable to reindex: "+question, e);
			operator.rollBack();
		}
	}

	public void index(Hypothesis hypothesis) {
		Privileged privileged = securityService.getAdminPrivileged();
		Operator operator = modelService.newOperator(privileged);
		try {
			User owner = modelService.getOwner(hypothesis, operator);
			if (owner!=null) {
				Document document = documentBuilder.build(hypothesis, operator, owner);
				index(hypothesis, owner, document);
			}
			operator.commit();
		} catch (EndUserException e) {
			log.error("Unable to reindex: "+hypothesis, e);
			operator.rollBack();
		}
	}

	public void index(Statement statement) {
		Privileged privileged = securityService.getAdminPrivileged();
		Operator operator = modelService.newOperator(privileged);
		try {
			User owner = modelService.getOwner(statement, operator);
			if (owner!=null) {

				Document doc = documentBuilder.build(statement, operator, owner);

				index(statement, owner, doc);
			}
			operator.commit();
		} catch (EndUserException e) {
			log.error("Unable to reindex: "+statement, e);
			operator.rollBack();
		}
	}

	private IndexManager getIndexManager(Privileged privileged) {
		return indexService.getIndex(getIndexName(privileged));
	}

	private String getIndexName(Privileged privileged) {
		return APP_READER_USER + privileged.getIdentity();
	}

	public void entityWasCreated(Entity entity) {
		check(entity);
	}

	public void entityWasUpdated(Entity entity) {
		check(entity);
	}

	private void check(Entity entity) {
		if (entity instanceof InternetAddress) {
			index((InternetAddress) entity);
		}
		if (entity instanceof Statement) {
			index((Statement) entity);
		}
		if (entity instanceof Question) {
			index((Question) entity);
		}
		if (entity instanceof Hypothesis) {
			index((Hypothesis) entity);
		}
	}

	public void entityWasDeleted(Entity entity) {
		/** @see ReaderIndexer#allPrivilegesWasRemoved */
	}

	@Override
	public void allPrivilegesWasRemoved(Item item, List<User> users) {
		if (item instanceof InternetAddress || item instanceof Statement || item instanceof Question || item instanceof Hypothesis) {
			for (User user : users) {
				try {
					getIndexManager(user).delete(item.getId());
				} catch (EndUserException e) {
					log.error("Unable to remove: "+item, e);
				}
			}
		}
	}

	public void relationWasCreated(Relation relation) {
		check(relation.getFrom());
		check(relation.getTo());
	}

	public void relationWasUpdated(Relation relation) {
		check(relation.getFrom());
		check(relation.getTo());
	}

	public void relationWasDeleted(Relation relation) {
		check(relation.getFrom());
		check(relation.getTo());
	}

	// Wiring...

	public void setDocumentBuilder(KnowledgeIndexDocumentBuilder documentBuilder) {
		this.documentBuilder = documentBuilder;
	}

	public void setIndexService(IndexService indexService) {
		this.indexService = indexService;
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setKnowledgeSolrIndexWriter(KnowledgeSolrIndexWriter knowledgeSolrIndexWriter) {
		this.knowledgeSolrIndexWriter = knowledgeSolrIndexWriter;
	}
}
