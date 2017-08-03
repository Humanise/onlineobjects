package dk.in2isoft.onlineobjects.modules.knowledge;

import java.util.ArrayList;
import java.util.List;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.parsing.HTMLDocument;
import dk.in2isoft.commons.xml.DocumentCleaner;
import dk.in2isoft.onlineobjects.apps.api.KnowledgeListRow;
import dk.in2isoft.onlineobjects.apps.reader.ReaderSearcher;
import dk.in2isoft.onlineobjects.apps.reader.index.ReaderQuery;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.SearchResult;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.ExplodingClusterFuckException;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.EmailAddress;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Hypothesis;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.Statement;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.information.SimpleContentExtractor;
import dk.in2isoft.onlineobjects.modules.networking.InternetAddressService;
import dk.in2isoft.onlineobjects.modules.user.MemberService;
import nu.xom.Document;

public class KnowledgeService {
	private ModelService modelService;
	private ReaderSearcher readerSearcher;
	private InternetAddressService internetAddressService;
	private MemberService memberService;

	public Question createQuestion(String text, User user) throws ModelException, SecurityException, IllegalRequestException {
		if (Strings.isBlank(text)) {
			throw new IllegalRequestException("The question is empty");
		}
		text = text.trim();
		Question question = new Question();
		question.setText(text);
		question.setName(text);
		modelService.createItem(question, user);
		return question;
	}

	public Hypothesis createHypothesis(String text, User user) throws ModelException, SecurityException, IllegalRequestException {
		if (Strings.isBlank(text)) {
			throw new IllegalRequestException("The hypothesis is empty");
		}
		text = text.trim();
		Hypothesis hypothesis = new Hypothesis();
		hypothesis.setText(text);
		hypothesis.setName(text);
		modelService.createItem(hypothesis, user);
		return hypothesis;
	}

	public HypothesisApiPerspective getHypothesisPerspective(Long id, User user) throws ModelException, ContentNotFoundException {
		Hypothesis hypothesis = modelService.getRequired(Hypothesis.class, id, user);
		HypothesisApiPerspective hypothesisPerspective = new HypothesisApiPerspective();
		hypothesisPerspective.setId(hypothesis.getId());
		hypothesisPerspective.setText(hypothesis.getText());
		return hypothesisPerspective;
	}

	public QuestionApiPerspective getQuestionPerspective(Long id, User user) throws ModelException, ContentNotFoundException {
		Question question = modelService.getRequired(Question.class, id, user);
		QuestionApiPerspective questionPerspective = new QuestionApiPerspective();
		questionPerspective.setId(question.getId());
		questionPerspective.setText(question.getText());
		List<Statement> answers = modelService.getParents(question, Relation.ANSWERS, Statement.class, user);
		List<StatementApiPerspective> answerPerspectives = new ArrayList<>();
		for (Statement answer : answers) {
			StatementApiPerspective statementPerspective = new StatementApiPerspective();
			statementPerspective.setId(answer.getId());
			statementPerspective.setText(answer.getText());
			List<Person> authors = modelService.getChildren(answer, Relation.KIND_COMMON_AUTHOR, Person.class, user);
			List<PersonApiPerspective> authorPerspectives = new ArrayList<>();
			for (Person person : authors) {
				PersonApiPerspective personPerspective = new PersonApiPerspective();
				personPerspective.setName(person.getFullName());
				personPerspective.setId(person.getId());
				authorPerspectives.add(personPerspective);
			}
			statementPerspective.setAuthors(authorPerspectives);

			List<InternetAddressApiPerspective> addressPerspectives = new ArrayList<InternetAddressApiPerspective>();
			List<InternetAddress> addresses = modelService.getParents(answer, Relation.KIND_STRUCTURE_CONTAINS, InternetAddress.class, user);
			for (InternetAddress address : addresses) {
				InternetAddressApiPerspective addressPerspective = new InternetAddressApiPerspective();
				addressPerspective.setId(address.getId());
				addressPerspective.setTitle(address.getName());
				addressPerspective.setUrl(address.getAddress());
				addressPerspectives.add(addressPerspective);
			}
			statementPerspective.setAddresses(addressPerspectives);
			answerPerspectives.add(statementPerspective);
		}
		questionPerspective.setAnswers(answerPerspectives);
		return questionPerspective;
	}

	public InternetAddressApiPerspective getAddressPerspective(Long id, User user) throws ModelException, ContentNotFoundException, SecurityException {
		InternetAddress address = modelService.getRequired(InternetAddress.class, id, user);
		InternetAddressApiPerspective addressPerspective = new InternetAddressApiPerspective();
		addressPerspective.setId(address.getId());
		addressPerspective.setTitle(address.getName());
		addressPerspective.setUrl(address.getAddress());
		
		HTMLDocument htmlDocument = internetAddressService.getHTMLDocument(address, user);
		SimpleContentExtractor extractor = new SimpleContentExtractor();
		Document extracted = extractor.extract(htmlDocument.getXOMDocument());
		DocumentCleaner cleaner = new DocumentCleaner();
		cleaner.clean(extracted);
		addressPerspective.setHtml(extracted.toXML());
		
		return addressPerspective;
	}
	
	public ProfileApiPerspective getProfile(User user) throws ModelException {
		ProfileApiPerspective profile = new ProfileApiPerspective();
		profile.setUsername(user.getUsername());
		EmailAddress email = memberService.getUsersPrimaryEmail(user, user);
		if (email!=null) {
			profile.setEmail(email.getAddress());			
		}
		Person person = memberService.getUsersPerson(user, user);
		if (person!=null) {
			profile.setFullName(person.getFullName());
		}
		Image photo = memberService.getUsersProfilePhoto(user, user);
		if (photo!=null) {
			profile.setProfilePhotoId(photo.getId());
		}
		return profile;
	}

	public SearchResult<KnowledgeListRow> search(ReaderQuery query, User user) throws ExplodingClusterFuckException, SecurityException {
		SearchResult<Entity> searchResult = readerSearcher.search(query, user);
		
		List<KnowledgeListRow> list = new ArrayList<>();
		for (Entity entity : searchResult.getList()) {
			KnowledgeListRow row = new KnowledgeListRow();
			row.id = entity.getId();
			row.type = entity.getClass().getSimpleName();
			if (entity instanceof InternetAddress) {
				InternetAddress address = (InternetAddress) entity;
				row.url = address.getAddress();
				row.text = address.getName();
			}
			else if (entity instanceof Statement) {
				row.text = ((Statement) entity).getText();
				Query<InternetAddress> q = Query.after(InternetAddress.class).to(entity, Relation.KIND_STRUCTURE_CONTAINS).as(user);
				InternetAddress addr = modelService.search(q).getFirst();
				if (addr != null) {
					row.url = addr.getAddress();
				}
			}
			else if (entity instanceof Question) {
				row.text = ((Question) entity).getText();
			}
			else if (entity instanceof Hypothesis) {
				row.text = ((Hypothesis) entity).getText();
			}
			list.add(row);
		}
		return new SearchResult<>(list, searchResult.getTotalCount());
	}
	
	// Wiring...
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
	public void setReaderSearcher(ReaderSearcher readerSearcher) {
		this.readerSearcher = readerSearcher;
	}
	
	public void setInternetAddressService(InternetAddressService internetAddressService) {
		this.internetAddressService = internetAddressService;
	}
	
	public void setMemberService(MemberService memberService) {
		this.memberService = memberService;
	}
}
