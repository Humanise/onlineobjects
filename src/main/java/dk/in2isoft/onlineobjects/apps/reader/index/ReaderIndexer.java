package dk.in2isoft.onlineobjects.apps.reader.index;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.DummyPrivileged;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.Results;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.events.ModelEventListener;
import dk.in2isoft.onlineobjects.core.events.ModelPrivilegesEventListener;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Hypothesis;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.model.Item;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.Pile;
import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.Statement;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.index.IndexDescription;
import dk.in2isoft.onlineobjects.modules.index.IndexManager;
import dk.in2isoft.onlineobjects.modules.index.IndexService;
import dk.in2isoft.onlineobjects.modules.index.Indexer;
import dk.in2isoft.onlineobjects.services.PileService;

public class ReaderIndexer implements ModelEventListener, ModelPrivilegesEventListener, Indexer {

	private static final String APP_READER_USER  = "app-reader-user-";

	private ReaderIndexDocumentBuilder documentBuilder;
	
	private IndexService indexService;
	private ModelService modelService;
	private PileService pileService;
	private SecurityService securityService;
	
	private static final Logger log = Logger.getLogger(ReaderIndexer.class);
	
	public void clear(Privileged privileged) throws EndUserException {
		getIndexManager(privileged).clear();
	}
	
	@Override
	public boolean is(IndexDescription description) {
		// TODO Auto-generated method stub
		return description.getName().startsWith(APP_READER_USER );
	}
	
	@Override
	public long getObjectCount(IndexDescription description) {
		DummyPrivileged privileged = new DummyPrivileged(description.getUserId());
		long count = 0;
		Class<?>[] types = {InternetAddress.class, Question.class, Statement.class, Hypothesis.class};
		for (Class<?> type : types) {
			count += modelService.count(Query.after(type).as(privileged));
		}
		return count;
	}
	
	@Override
	public List<IndexDescription> getIndexInstances() {
		Query<User> query = Query.after(User.class);
		return modelService.search(query).getList().stream().map(user -> {
			IndexDescription desc = new IndexDescription();
			desc.setName(getIndexName(user));
			desc.setUserId(user.getId());
			return desc;
		}).collect(Collectors.toList());
	}
	
	public void reIndex(Privileged privileged) throws EndUserException {

		clear(privileged);
		{
			Query<InternetAddress> query = Query.after(InternetAddress.class).as(privileged);
			Results<InternetAddress> scroll = modelService.scroll(query);
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
			Query<Statement> query = Query.after(Statement.class).as(privileged);
			Results<Statement> scroll = modelService.scroll(query);
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
			Query<Question> query = Query.after(Question.class).as(privileged);
			Results<Question> scroll = modelService.scroll(query);
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
			Query<Hypothesis> query = Query.after(Hypothesis.class).as(privileged);
			Results<Hypothesis> scroll = modelService.scroll(query);
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
		try {
			User owner = modelService.getOwner(address, securityService.getAdminPrivileged());
			if (owner!=null) {
				Document document = documentBuilder.build(address);
				log.debug("Re-indexing : "+address);
				getIndexManager(owner).update(address, document);
			}
		} catch (EndUserException e) {
			log.error("Unable to reindex: "+address, e);
		}
	}
	
	public void index(Question question) {
		try {
			User owner = modelService.getOwner(question, securityService.getAdminPrivileged());
			if (owner!=null) {
				StringBuilder text = new StringBuilder();
				if (question.getText()!=null) {
					text.append(question.getText());
				}
				Document doc = new Document();
				doc.add(new TextField("title", Strings.asNonBlank(question.getName(),"blank"), Field.Store.YES));
				indexStatus(doc, question, owner);

				Query<Person> authors = Query.of(Person.class).from(question, Relation.KIND_COMMON_AUTHOR).as(owner);
				List<Person> people = modelService.list(authors);
				for (Person person : people) {
					doc.add(new StringField("author", String.valueOf(person.getId()), Field.Store.NO));
					text.append(" ").append(person.getFullName());
				}
				doc.add(new TextField("text", Strings.asNonBlank(text.toString(),""), Field.Store.NO));

				getIndexManager(owner).update(question, doc);
			}
		} catch (EndUserException e) {
			log.error("Unable to reindex: "+question, e);
		}
	}
	
	public void index(Hypothesis hypothesis) {
		try {
			User owner = modelService.getOwner(hypothesis, securityService.getAdminPrivileged());
			if (owner!=null) {
				StringBuilder text = new StringBuilder();
				if (hypothesis.getText()!=null) {
					text.append(hypothesis.getText());
				}
				Document doc = new Document();
				doc.add(new TextField("title", Strings.asNonBlank(hypothesis.getName(),"blank"), Field.Store.YES));
				indexStatus(doc, hypothesis, owner);

				Query<Person> authors = Query.of(Person.class).from(hypothesis, Relation.KIND_COMMON_AUTHOR).as(owner);
				List<Person> people = modelService.list(authors);
				for (Person person : people) {
					doc.add(new StringField("author", String.valueOf(person.getId()), Field.Store.NO));
					text.append(" ").append(person.getFullName());
				}
				doc.add(new TextField("text", Strings.asNonBlank(text.toString(),""), Field.Store.NO));

				getIndexManager(owner).update(hypothesis, doc);
			}
		} catch (EndUserException e) {
			log.error("Unable to reindex: "+hypothesis, e);
		}
	}
	
	public void index(Statement statement) {
		try {
			User owner = modelService.getOwner(statement, securityService.getAdminPrivileged());
			if (owner!=null) {
				StringBuilder text = new StringBuilder();
				if (statement.getText()!=null) {
					text.append(statement.getText());
				}
				Document doc = new Document();
				doc.add(new TextField("title", Strings.asNonBlank(statement.getName(),"blank"), Field.Store.YES));
				indexStatus(doc, statement, owner);

				Query<Person> authors = Query.of(Person.class).from(statement, Relation.KIND_COMMON_AUTHOR).as(owner);
				List<Person> people = modelService.list(authors);
				for (Person person : people) {
					doc.add(new StringField("author", String.valueOf(person.getId()), Field.Store.NO));
					text.append(" ").append(person.getFullName());
				}
				doc.add(new TextField("text", Strings.asNonBlank(text.toString(),""), Field.Store.NO));

				getIndexManager(owner).update(statement, doc);
			}
		} catch (EndUserException e) {
			log.error("Unable to reindex: "+statement, e);
		}
	}
	
	private void indexStatus(Document doc, Entity entity, User owner) throws ModelException, SecurityException {

		Pile inbox = pileService.getOrCreatePileByRelation(owner, Relation.KIND_SYSTEM_USER_INBOX);
		Pile favorites = pileService.getOrCreatePileByRelation(owner, Relation.KIND_SYSTEM_USER_FAVORITES);
		
		boolean inboxed = false;
		boolean favorited = false;
		
		List<Pile> piles = modelService.getParents(entity, Pile.class, owner);
		for (Pile pile : piles) {
			if (pile.getId()==inbox.getId()) {
				inboxed = true;
			} else if (pile.getId()==favorites.getId()) {
				favorited = true;
			}
		}
		doc.add(new TextField("inbox", inboxed ? "yes" : "no", Field.Store.YES));
		doc.add(new TextField("favorite", favorited ? "yes" : "no", Field.Store.YES));
		doc.add(new LongField("updated", entity.getUpdated().getTime(), Field.Store.YES));
		doc.add(new LongField("created", entity.getCreated().getTime(), Field.Store.YES));
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
	
	public void setDocumentBuilder(ReaderIndexDocumentBuilder documentBuilder) {
		this.documentBuilder = documentBuilder;
	}
	
	public void setIndexService(IndexService indexService) {
		this.indexService = indexService;
	}
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
	public void setPileService(PileService pileService) {
		this.pileService = pileService;
	}
	
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
}
