package dk.in2isoft.onlineobjects.apps.knowledge.index;

import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Hypothesis;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.Pile;
import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.Statement;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.model.Word;
import dk.in2isoft.onlineobjects.modules.networking.InternetAddressService;
import dk.in2isoft.onlineobjects.services.PileService;

public class KnowledgeIndexDocumentBuilder {

	private ModelService modelService;
	private PileService pileService;
	private InternetAddressService internetAddressService;

	public Document build(InternetAddress address, Operator operator, User owner) throws EndUserException {

		Document doc = new Document();
		doc.add(new TextField("title", Strings.asNonBlank(address.getName(), "blank"), Field.Store.YES));
		StringBuilder text = new StringBuilder();
		String body = internetAddressService.getText(address, operator);
		if (body != null) {
			text.append(body);
		}
		text.append(indexStatus(doc, address, owner, operator));

		doc.add(new TextField("text", Strings.asNonBlank(text.toString(), ""), Field.Store.NO));


		return doc;
	}

	public Document build(Question question, Operator operator, User owner) throws ModelException, SecurityException {
		StringBuilder text = new StringBuilder();
		if (question.getText() != null) {
			text.append(question.getText());
		}
		Document doc = new Document();
		doc.add(new TextField("title", Strings.asNonBlank(question.getName(), "blank"), Field.Store.YES));
		text.append(indexStatus(doc, question, owner, operator));

		doc.add(new TextField("text", Strings.asNonBlank(text.toString(), ""), Field.Store.NO));
		return doc;
	}

	public Document build(Statement statement, Operator operator, User owner) throws ModelException, SecurityException {
		StringBuilder text = new StringBuilder();
		if (statement.getText() != null) {
			text.append(statement.getText());
		}
		Document doc = new Document();
		doc.add(new TextField("title", Strings.asNonBlank(statement.getName(), "blank"), Field.Store.YES));
		text.append(indexStatus(doc, statement, owner, operator));
		doc.add(new TextField("text", Strings.asNonBlank(text.toString(), ""), Field.Store.NO));
		return doc;
	}

	public Document build(Hypothesis hypothesis, Operator operator, User owner) throws ModelException, SecurityException {
		StringBuilder text = new StringBuilder();
		if (hypothesis.getText()!=null) {
			text.append(hypothesis.getText());
		}
		Document doc = new Document();
		doc.add(new TextField("title", Strings.asNonBlank(hypothesis.getName(),"blank"), Field.Store.YES));
		text.append(indexStatus(doc, hypothesis, owner, operator));
		doc.add(new TextField("text", Strings.asNonBlank(text.toString(),""), Field.Store.NO));
		return doc;
	}

	private String indexStatus(Document doc, Entity entity, User owner, Operator operator)
			throws ModelException, SecurityException {

		Pile inbox = pileService.getOrCreatePileByRelation(owner, Relation.KIND_SYSTEM_USER_INBOX, operator);
		Pile favorites = pileService.getOrCreatePileByRelation(owner, Relation.KIND_SYSTEM_USER_FAVORITES, operator);

		boolean inboxed = false;
		boolean favorited = false;

		List<Pile> piles = modelService.getParents(entity, Pile.class, operator);
		for (Pile pile : piles) {
			if (pile.getId() == inbox.getId()) {
				inboxed = true;
			} else if (pile.getId() == favorites.getId()) {
				favorited = true;
			}
		}
		doc.add(new TextField("inbox", inboxed ? "yes" : "no", Field.Store.YES));
		doc.add(new TextField("favorite", favorited ? "yes" : "no", Field.Store.YES));
		doc.add(new LongField("updated", entity.getUpdated().getTime(), Field.Store.YES));
		doc.add(new LongField("created", entity.getCreated().getTime(), Field.Store.YES));


		List<Word> words = modelService.getChildren(entity, Word.class, operator);
		StringBuilder wordText = new StringBuilder();
		for (Word word : words) {
			if (wordText.length() > 0) {
				wordText.append(" ");
			}
			wordText.append(word.getText());
			doc.add(new StringField("word", String.valueOf(word.getId()), Field.Store.NO));
		}
		doc.add(new TextField("words", wordText.toString(), Field.Store.NO));

		StringBuilder text = new StringBuilder();
		Query<Person> authors = Query.of(Person.class).from(entity, Relation.KIND_COMMON_AUTHOR).as(owner);
		List<Person> people = modelService.list(authors, operator);
		for (Person person : people) {
			doc.add(new StringField("author", String.valueOf(person.getId()), Field.Store.NO));
			text.append(" ").append(person.getFullName());
		}
		return text.toString();
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public void setPileService(PileService pilService) {
		this.pileService = pilService;
	}

	public void setInternetAddressService(InternetAddressService internetAddressService) {
		this.internetAddressService = internetAddressService;
	}
}
