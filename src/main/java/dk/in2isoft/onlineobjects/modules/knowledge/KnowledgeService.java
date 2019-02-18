package dk.in2isoft.onlineobjects.modules.knowledge;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.Nullable;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.in2igui.data.ItemData;
import dk.in2isoft.onlineobjects.apps.api.KnowledgeListRow;
import dk.in2isoft.onlineobjects.apps.knowledge.KnowledgeSearcher;
import dk.in2isoft.onlineobjects.apps.knowledge.index.KnowledgeQuery;
import dk.in2isoft.onlineobjects.apps.knowledge.perspective.CategorizableViewPerspective;
import dk.in2isoft.onlineobjects.apps.knowledge.perspective.HypothesisEditPerspective;
import dk.in2isoft.onlineobjects.apps.knowledge.perspective.InternetAddressViewPerspective;
import dk.in2isoft.onlineobjects.apps.knowledge.perspective.InternetAddressViewPerspectiveBuilder;
import dk.in2isoft.onlineobjects.apps.knowledge.perspective.InternetAddressViewPerspectiveBuilder.Settings;
import dk.in2isoft.onlineobjects.apps.knowledge.perspective.QuestionEditPerspective;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
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
import dk.in2isoft.onlineobjects.model.Pile;
import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.Statement;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.networking.InternetAddressService;
import dk.in2isoft.onlineobjects.modules.user.MemberService;
import dk.in2isoft.onlineobjects.services.PileService;

public class KnowledgeService {
	private ModelService modelService;
	private KnowledgeSearcher readerSearcher;
	private InternetAddressService internetAddressService;
	private PileService pileService; 
	private MemberService memberService;
	private InternetAddressViewPerspectiveBuilder internetAddressViewPerspectiveBuilder;

	public Question createQuestion(String text, Privileged user) throws ModelException, SecurityException, IllegalRequestException {
		if (Strings.isBlank(text)) {
			throw new IllegalRequestException("The question is empty");
		}
		text = text.trim();
		Question question = new Question();
		question.setText(text);
		question.setName(text);
		modelService.create(question, user);
		return question;
	}

	public InternetAddress createInternetAddress(String url, User user) throws ModelException, SecurityException, IllegalRequestException, ContentNotFoundException {
		return internetAddressService.create(url, null, user);
	}

	public InternetAddress createInternetAddress(AddressRequest request) throws ModelException, SecurityException, IllegalRequestException, ContentNotFoundException {
		String url = request.getUrl();
		User user = request.getUser();
		Long questionId = request.getQuestionId();
		String title = request.getTitle();
		String quote = request.getQuote();
		InternetAddress internetAddress = internetAddressService.create(url, title, user);

		if (Strings.isNotBlank(quote)) {
			Statement statement = addStatementToInternetAddress(quote, internetAddress, user);
			if (questionId != null) {
				Question question = modelService.getRequired(Question.class, questionId, user);
				Optional<Relation> found = modelService.find().relations(user).from(statement).to(question).withKind(Relation.ANSWERS).first();
				if (!found.isPresent()) {
					modelService.createRelation(statement, question, Relation.ANSWERS, user);
					modelService.commit();
				}
			}

		}

		return internetAddress;
	}

	public void deleteQuestion(Long id, Privileged privileged) throws ModelException, ContentNotFoundException, SecurityException {
		Question question = modelService.getRequired(Question.class, id, privileged);
		modelService.delete(question, privileged);
	}

	public void deleteQuestion(Long id, Operator privileged) throws ModelException, ContentNotFoundException, SecurityException {
		Question question = modelService.getRequired(Question.class, id, privileged);
		modelService.delete(question, privileged);
	}

	public void deleteStatement(Long id, Privileged privileged) throws ModelException, ContentNotFoundException, SecurityException {
		Statement statement = modelService.getRequired(Statement.class, id, privileged);
		modelService.delete(statement, privileged);
	}

	public void deleteHypothesis(Long id, Privileged privileged) throws ModelException, ContentNotFoundException, SecurityException {
		Hypothesis hypothesis = modelService.getRequired(Hypothesis.class, id, privileged);
		modelService.delete(hypothesis, privileged);
	}

	public void deleteInternetAddress(Long id, Privileged privileged) throws ModelException, ContentNotFoundException, SecurityException {
		InternetAddress address = modelService.getRequired(InternetAddress.class, id, privileged);
		List<Statement> children = modelService.getChildren(address, Relation.KIND_STRUCTURE_CONTAINS, Statement.class, privileged);

		modelService.delete(address, privileged);

		for (Statement htmlPart : children) {
			modelService.delete(htmlPart, privileged);
		}

	}

	public void categorize(Entity entity, CategorizableViewPerspective perspective, User user) throws ModelException, SecurityException {
		Pile inbox = pileService.getOrCreatePileByRelation(user, Relation.KIND_SYSTEM_USER_INBOX);
		Pile favorites = pileService.getOrCreatePileByRelation(user, Relation.KIND_SYSTEM_USER_FAVORITES);

		List<Pile> piles = modelService.getParents(entity, Pile.class, user);
		for (Pile pile : piles) {
			if (pile.getId() == inbox.getId()) {
				perspective.setInbox(true);
			} else if (pile.getId() == favorites.getId()) {
				perspective.setFavorite(true);
			}
		}
	}

	public void categorize(Entity entity, CategorizableViewPerspective perspective, Operator operator) throws ModelException, SecurityException, ContentNotFoundException {
		User user = modelService.getRequired(User.class, operator.getIdentity(), operator);

		Pile inbox = pileService.getOrCreatePileByRelation(user, Relation.KIND_SYSTEM_USER_INBOX);
		Pile favorites = pileService.getOrCreatePileByRelation(user, Relation.KIND_SYSTEM_USER_FAVORITES);

		List<Pile> piles = modelService.getParents(entity, Pile.class, operator);
		for (Pile pile : piles) {
			if (pile.getId() == inbox.getId()) {
				perspective.setInbox(true);
			} else if (pile.getId() == favorites.getId()) {
				perspective.setFavorite(true);
			}
		}
	}

	public Statement addStatementToInternetAddress(String text, Long internetAddressId, Privileged user) throws ModelException, ContentNotFoundException, SecurityException, IllegalRequestException {
		InternetAddress address = modelService.getRequired(InternetAddress.class, internetAddressId, user);
		return addStatementToInternetAddress(text, address, user);
	}
	
	public Statement addStatementToInternetAddress(String text, InternetAddress address, Privileged user) throws ModelException, ContentNotFoundException, SecurityException, IllegalRequestException {
		if (Strings.isBlank(text)) {
			throw new IllegalRequestException("No text");
		}
		text = text.trim();
		Query<Statement> existingQuery = Query.after(Statement.class).withField("text", text).as(user).from(address, Relation.KIND_STRUCTURE_CONTAINS);
		if (modelService.count(existingQuery) == 0) {
			Statement part = new Statement();
			part.setName(StringUtils.abbreviate(text, 50));
			part.setText(text);
			modelService.create(part, user);
			modelService.createRelation(address, part, Relation.KIND_STRUCTURE_CONTAINS, user);
			return part;
		}
		return null;
	}

	public Hypothesis createHypothesis(String text, Privileged user) throws ModelException, SecurityException, IllegalRequestException {
		if (Strings.isBlank(text)) {
			throw new IllegalRequestException("The hypothesis is empty");
		}
		text = text.trim();
		Hypothesis hypothesis = new Hypothesis();
		hypothesis.setText(text);
		hypothesis.setName(text);
		modelService.create(hypothesis, user);
		return hypothesis;
	}

	public int compare(Relation a, Relation b) {
		float comp = a.getPosition() - b.getPosition();
		if (comp == 0) {
			return (int) (a.getId() - b.getId());
		}
		return (int)comp;
	}

	public HypothesisApiPerspective getHypothesisPerspective(Long id, User user) throws ModelException, ContentNotFoundException, SecurityException {
		Hypothesis hypothesis = modelService.getRequired(Hypothesis.class, id, user);
		HypothesisApiPerspective perspective = new HypothesisApiPerspective();
		perspective.setId(hypothesis.getId());
		perspective.setText(hypothesis.getText());

		List<Relation> supports = modelService.find().relations(user).from(Statement.class).to(hypothesis).withKind(Relation.SUPPORTS).list();
		supports.sort((a,b) -> compare(a,b));
		List<StatementApiPerspective> supportsPerspectives = new ArrayList<>();
		for (Relation relation : supports) {
			Statement c = (Statement) relation.getFrom();
			StatementApiPerspective statementPerspective = new StatementApiPerspective();
			statementPerspective.setId(c.getId());
			statementPerspective.setText(c.getText());
			populateStatement(user, c, statementPerspective);
			supportsPerspectives.add(statementPerspective);
		}
		perspective.setSupporting(supportsPerspectives);
		List<Relation> contradicts = modelService.find().relations(user).from(Statement.class).to(hypothesis).withKind(Relation.CONTRADTICS).list();
		contradicts.sort((a,b) -> compare(a,b));
		List<StatementApiPerspective> contradictsPerspectives = new ArrayList<>();
		for (Relation relation : contradicts) {
			Statement c = (Statement) relation.getFrom();
			StatementApiPerspective statementPerspective = new StatementApiPerspective();
			statementPerspective.setId(c.getId());
			statementPerspective.setText(c.getText());
			populateStatement(user, c, statementPerspective);
			contradictsPerspectives.add(statementPerspective);
		}
		perspective.setContradicting(contradictsPerspectives);
		categorize(hypothesis, perspective, user);
		return perspective;
	}

	public StatementApiPerspective getStatementPerspective(Long id, User user) throws ModelException, ContentNotFoundException, SecurityException {
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
		List<Relation> questions = modelService.find().relations(user).from(statement).to(Question.class).withKind(Relation.ANSWERS).list();
		questions.sort((a,b) -> compare(a, b));
		perspective.setQuestions(questions.stream().map(relation -> {
			Question question = (Question) relation.getTo();
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
		categorize(statement, perspective, user);
		return perspective;
	}

	public Statement addPersonalStatement(String text, User user) throws ModelException, SecurityException {
		Statement statement = new Statement();
		statement.setText(text);
		statement.setName(text);
		modelService.create(statement, user);
		Person person = memberService.getUsersPerson(user, user);
		if (person != null) {
			modelService.createRelation(statement, person, Relation.KIND_COMMON_AUTHOR, user);
		}
		return statement;
	}

	public QuestionApiPerspective getQuestionPerspective(Long id, Operator operator) throws ModelException, ContentNotFoundException, SecurityException {
		Question question = modelService.getRequired(Question.class, id, operator);
		QuestionApiPerspective questionPerspective = new QuestionApiPerspective();
		questionPerspective.setId(question.getId());
		questionPerspective.setText(question.getText());

		List<Relation> answers = modelService.find().relations(operator).from(Statement.class).to(question).withKind(Relation.ANSWERS).list();
		answers.sort((a,b) -> compare(a, b));
		List<StatementApiPerspective> answerPerspectives = new ArrayList<>();
		for (Relation relation : answers) {
			Statement answer = (Statement) relation.getFrom();
			StatementApiPerspective statementPerspective = new StatementApiPerspective();
			statementPerspective.setId(answer.getId());
			statementPerspective.setText(answer.getText());
			populateStatement(operator, answer, statementPerspective);
			answerPerspectives.add(statementPerspective);
		}
		questionPerspective.setAnswers(answerPerspectives);
		categorize(question, questionPerspective, operator);
		return questionPerspective;
	}

	@Deprecated
	public QuestionApiPerspective getQuestionPerspective(Long id, User user) throws ModelException, ContentNotFoundException, SecurityException {
		Question question = modelService.getRequired(Question.class, id, user);
		QuestionApiPerspective questionPerspective = new QuestionApiPerspective();
		questionPerspective.setId(question.getId());
		questionPerspective.setText(question.getText());

		List<Relation> answers = modelService.find().relations(user).from(Statement.class).to(question).withKind(Relation.ANSWERS).list();
		answers.sort((a,b) -> compare(a, b));
		List<StatementApiPerspective> answerPerspectives = new ArrayList<>();
		for (Relation relation : answers) {
			Statement answer = (Statement) relation.getFrom();
			StatementApiPerspective statementPerspective = new StatementApiPerspective();
			statementPerspective.setId(answer.getId());
			statementPerspective.setText(answer.getText());
			populateStatement(user, answer, statementPerspective);
			answerPerspectives.add(statementPerspective);
		}
		questionPerspective.setAnswers(answerPerspectives);
		categorize(question, questionPerspective, user);
		return questionPerspective;
	}

	private void populateStatement(Privileged user, Statement answer, StatementApiPerspective statementPerspective)
			throws ModelException {
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
	}

	private void populateStatement(Operator operator, Statement answer, StatementApiPerspective statementPerspective)
			throws ModelException {
		List<Person> authors = modelService.getChildren(answer, Relation.KIND_COMMON_AUTHOR, Person.class, operator);
		List<PersonApiPerspective> authorPerspectives = new ArrayList<>();
		for (Person person : authors) {
			PersonApiPerspective personPerspective = new PersonApiPerspective();
			personPerspective.setName(person.getFullName());
			personPerspective.setId(person.getId());
			authorPerspectives.add(personPerspective);
		}
		statementPerspective.setAuthors(authorPerspectives);

		List<InternetAddressApiPerspective> addressPerspectives = new ArrayList<InternetAddressApiPerspective>();
		List<InternetAddress> addresses = modelService.getParents(answer, Relation.KIND_STRUCTURE_CONTAINS, InternetAddress.class, operator);
		for (InternetAddress address : addresses) {
			InternetAddressApiPerspective addressPerspective = new InternetAddressApiPerspective();
			addressPerspective.setId(address.getId());
			addressPerspective.setTitle(address.getName());
			addressPerspective.setUrl(address.getAddress());
			addressPerspectives.add(addressPerspective);
		}
		statementPerspective.setAddresses(addressPerspectives);
	}

	public void updateQuestion(Question dummy, Operator operator) throws ModelException, SecurityException {
		@Nullable
		Question question = modelService.get(Question.class, dummy.getId(), operator);
		question.setText(dummy.getText());
		question.setName(dummy.getText());
		modelService.update(question, operator);
	}

	public void updateQuestion(Question dummy, Privileged privileged) throws ModelException, SecurityException {
		@Nullable
		Question question = modelService.get(Question.class, dummy.getId(), privileged);
		question.setText(dummy.getText());
		question.setName(dummy.getText());
		modelService.update(question, privileged);
	}

	public void updateStatement(Statement dummy, Privileged privileged) throws ModelException, SecurityException {
		Statement statement = modelService.get(Statement.class, dummy.getId(), privileged);
		statement.setText(dummy.getText());
		statement.setName(dummy.getText());
		modelService.update(statement, privileged);		
	}

	public void updateHypothesis(Hypothesis dummy, Privileged privileged) throws ModelException, SecurityException {
		Hypothesis hypothesis = modelService.get(Hypothesis.class, dummy.getId(), privileged);
		hypothesis.setText(dummy.getText());
		hypothesis.setName(dummy.getText());
		modelService.update(hypothesis, privileged);		
	}

	public InternetAddressApiPerspective getAddressPerspective(Long id, Privileged session) throws ModelException, ContentNotFoundException, SecurityException, IllegalRequestException, ExplodingClusterFuckException {
		InternetAddress address = modelService.getRequired(InternetAddress.class, id, session);
		User user = modelService.getRequired(User.class, session.getIdentity(), session);
		return getAddressPerspective(address, user);
	}

	public InternetAddressApiPerspective getAddressPerspective(InternetAddress address, User user) throws ModelException, ContentNotFoundException, SecurityException, IllegalRequestException, ExplodingClusterFuckException {
		InternetAddressApiPerspective addressPerspective = new InternetAddressApiPerspective();
		addressPerspective.setId(address.getId());
		addressPerspective.setTitle(address.getName());
		addressPerspective.setUrl(address.getAddress());
		
		Settings settings = new Settings();
		settings.setExtractionAlgorithm("OnlineObjects"); // TODO Should be a constant
		
		InternetAddressViewPerspective internetAddressViewPerspective = internetAddressViewPerspectiveBuilder.build(address.getId(), settings, user);
		
		addressPerspective.setHtml(internetAddressViewPerspective.getFormatted());
		addressPerspective.setText(internetAddressViewPerspective.getText());
		categorize(address, addressPerspective, user);
		return addressPerspective;
	}
	
	public QuestionEditPerspective getQuestionEditPerspective(Long id, UserSession session) throws ModelException, ContentNotFoundException {
		@Nullable
		Question statement = modelService.get(Question.class, id, session);
		if (statement == null) {
			throw new ContentNotFoundException(Question.class, id);
		}
		QuestionEditPerspective perspective = new QuestionEditPerspective();
		perspective.setText(statement.getText());
		perspective.setId(id);
		List<Person> people = getAuthors(statement, session);
		perspective.setAuthors(buildItemData(people));
		return perspective;
	}

	public HypothesisEditPerspective getHypothesisEditPerspective(Long id, Privileged session) throws ModelException, ContentNotFoundException {
		@Nullable
		Hypothesis hypothesis = modelService.get(Hypothesis.class, id, session);
		if (hypothesis == null) {
			throw new ContentNotFoundException(Question.class, id);
		}
		HypothesisEditPerspective perspective = new HypothesisEditPerspective();
		perspective.setText(hypothesis.getText());
		perspective.setId(id);
		List<Person> people = getAuthors(hypothesis, session);
		perspective.setAuthors(buildItemData(people));
		return perspective;
	}

	private List<ItemData> buildItemData(List<Person> people) {
		return people.stream().map((Person p) -> {
			ItemData option = new ItemData();
			option.setId(p.getId());
			option.setText(p.getFullName());
			option.setIcon(p.getIcon());
			return option;
		}).collect(Collectors.toList());
	}

	public List<Person> getAuthors(Entity entity, Privileged privileged) {
		Query<Person> query = Query.of(Person.class).from(entity, Relation.KIND_COMMON_AUTHOR).as(privileged);
		List<Person> people = modelService.list(query);
		return people;
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

	public SearchResult<KnowledgeListRow> search(KnowledgeQuery query, Privileged user) throws ExplodingClusterFuckException, SecurityException, ModelException {
		return readerSearcher.searchOptimized(query, user);
	}
	
	// Wiring...
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
	public void setReaderSearcher(KnowledgeSearcher readerSearcher) {
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

	public void setPileService(PileService pileService) {
		this.pileService = pileService;
	}

}
