package dk.in2isoft.onlineobjects.modules.knowledge;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.Nullable;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.apps.api.KnowledgeListRow;
import dk.in2isoft.onlineobjects.apps.reader.ReaderSearcher;
import dk.in2isoft.onlineobjects.apps.reader.index.ReaderQuery;
import dk.in2isoft.onlineobjects.apps.reader.perspective.InternetAddressViewPerspective;
import dk.in2isoft.onlineobjects.apps.reader.perspective.InternetAddressViewPerspectiveBuilder;
import dk.in2isoft.onlineobjects.apps.reader.perspective.InternetAddressViewPerspectiveBuilder.Settings;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.SearchResult;
import dk.in2isoft.onlineobjects.core.UserSession;
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
import dk.in2isoft.onlineobjects.modules.networking.InternetAddressService;
import dk.in2isoft.onlineobjects.modules.user.MemberService;

public class KnowledgeService {
	private ModelService modelService;
	private ReaderSearcher readerSearcher;
	private InternetAddressService internetAddressService;
	private MemberService memberService;
	private InternetAddressViewPerspectiveBuilder internetAddressViewPerspectiveBuilder;

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

	public void deleteQuestion(Long id, User user) throws ModelException, ContentNotFoundException, SecurityException {
		Question question = modelService.getRequired(Question.class, id, user);
		modelService.deleteEntity(question, user);
	}

	public void deleteStatement(Long id, User user) throws ModelException, ContentNotFoundException, SecurityException {
		Statement statement = modelService.getRequired(Statement.class, id, user);
		modelService.deleteEntity(statement, user);
	}

	public Statement addStatementToInternetAddress(String text, Long internetAddressId, User user) throws ModelException, ContentNotFoundException, SecurityException, IllegalRequestException {
		InternetAddress address = modelService.getRequired(InternetAddress.class, internetAddressId, user);
		return addStatementToInternetAddress(text, address, user);
	}
	
	public Statement addStatementToInternetAddress(String text, InternetAddress address, User user) throws ModelException, ContentNotFoundException, SecurityException, IllegalRequestException {
		if (Strings.isBlank(text)) {
			throw new IllegalRequestException("No text");
		}
		text = text.trim();
		Query<Statement> existingQuery = Query.after(Statement.class).withField("text", text).as(user).from(address, Relation.KIND_STRUCTURE_CONTAINS);
		if (modelService.count(existingQuery) == 0) {
			Statement part = new Statement();
			part.setName(StringUtils.abbreviate(text, 50));
			part.setText(text);
			modelService.createItem(part, user);
			modelService.createRelation(address, part, Relation.KIND_STRUCTURE_CONTAINS, user);
			return part;
		}
		return null;
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
		HypothesisApiPerspective perspective = new HypothesisApiPerspective();
		perspective.setId(hypothesis.getId());
		perspective.setText(hypothesis.getText());

		List<Statement> supports = modelService.getParents(hypothesis, Relation.SUPPORTS, Statement.class, user);
		perspective.setSupporting(supports.stream().map(statement -> {
			StatementApiPerspective statementPerspective = new StatementApiPerspective();
			statementPerspective.setId(statement.getId());
			statementPerspective.setText(statement.getText());
			return statementPerspective;
		}).collect(Collectors.toList()));
		List<Statement> contradicts = modelService.getParents(hypothesis, Relation.CONTRADTICS, Statement.class, user);
		perspective.setContradicting(contradicts.stream().map(address -> {
			StatementApiPerspective addressPerspective = new StatementApiPerspective();
			addressPerspective.setId(address.getId());
			addressPerspective.setText(address.getText());
			return addressPerspective;
		}).collect(Collectors.toList()));
		return perspective;
	}

	public StatementApiPerspective getStatementPerspective(Long id, User user) throws ModelException, ContentNotFoundException {
		Statement statement = modelService.getRequired(Statement.class, id, user);
		StatementApiPerspective perspective = new StatementApiPerspective();
		perspective.setId(statement.getId());
		perspective.setText(statement.getText());
		List<InternetAddress> answers = modelService.getParents(statement, Relation.KIND_STRUCTURE_CONTAINS, InternetAddress.class, user);
		perspective.setAddresses(answers.stream().map(address -> {
			InternetAddressApiPerspective addressPerspective = new InternetAddressApiPerspective();
			addressPerspective.setId(address.getId());
			addressPerspective.setTitle(address.getName());
			addressPerspective.setUrl(address.getAddress());
			return addressPerspective;
		}).collect(Collectors.toList()));
		List<Question> questions = modelService.getChildren(statement, Relation.ANSWERS, Question.class, user);
		perspective.setQuestions(questions.stream().map(question -> {
			QuestionApiPerspective p = new QuestionApiPerspective();
			p.setId(question.getId());
			p.setText(question.getText());
			return p;
		}).collect(Collectors.toList()));
		List<Person> authors = modelService.getChildren(statement, Relation.KIND_COMMON_AUTHOR, Person.class, user);
		List<PersonApiPerspective> authorPerspectives = new ArrayList<>();
		for (Person person : authors) {
			PersonApiPerspective personPerspective = new PersonApiPerspective();
			personPerspective.setName(person.getFullName());
			personPerspective.setId(person.getId());
			authorPerspectives.add(personPerspective);
		}
		perspective.setAuthors(authorPerspectives);
		return perspective;
	}

	public Statement addPersonalStatement(String text, User user) throws ModelException, SecurityException {
		Statement statement = new Statement();
		statement.setText(text);
		statement.setName(text);
		modelService.createItem(statement, user);
		Person person = memberService.getUsersPerson(user, user);
		if (person != null) {
			modelService.createRelation(statement, person, Relation.KIND_COMMON_AUTHOR, user);
		}
		return statement;
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

	public void updateQuestion(Question dummy, Privileged privileged) throws ModelException, SecurityException {
		@Nullable
		Question question = modelService.get(Question.class, dummy.getId(), privileged);
		question.setText(dummy.getText());
		question.setName(dummy.getText());
		modelService.updateItem(question, privileged);
	}

	public void updateStatement(Statement dummy, Privileged privileged) throws ModelException, SecurityException {
		Statement statement = modelService.get(Statement.class, dummy.getId(), privileged);
		statement.setText(dummy.getText());
		statement.setName(dummy.getText());
		modelService.updateItem(statement, privileged);		
	}

	public InternetAddressApiPerspective getAddressPerspective(Long id, UserSession session) throws ModelException, ContentNotFoundException, SecurityException, IllegalRequestException, ExplodingClusterFuckException {
		InternetAddress address = modelService.getRequired(InternetAddress.class, id, session);
		return getAddressPerspective(address, session);
	}

	public InternetAddressApiPerspective getAddressPerspective(InternetAddress address, UserSession session) throws ModelException, ContentNotFoundException, SecurityException, IllegalRequestException, ExplodingClusterFuckException {
		InternetAddressApiPerspective addressPerspective = new InternetAddressApiPerspective();
		addressPerspective.setId(address.getId());
		addressPerspective.setTitle(address.getName());
		addressPerspective.setUrl(address.getAddress());
		
		Settings settings = new Settings();
		settings.setExtractionAlgorithm("OnlineObjects"); // TODO Should be a constant
		
		InternetAddressViewPerspective internetAddressViewPerspective = internetAddressViewPerspectiveBuilder.build(address.getId(), settings, session);
		
		addressPerspective.setHtml(internetAddressViewPerspective.getFormatted());
		addressPerspective.setText(internetAddressViewPerspective.getText());
		
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
	
	public void setInternetAddressViewPerspectiveBuilder(InternetAddressViewPerspectiveBuilder internetAddressViewPerspectiveBuilder) {
		this.internetAddressViewPerspectiveBuilder = internetAddressViewPerspectiveBuilder;
	}


}
