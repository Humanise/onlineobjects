package dk.in2isoft.onlineobjects.modules.knowledge;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.SearchResult;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.ExplodingClusterFuckException;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.EmailAddress;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Hypothesis;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.model.Item;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.Pile;
import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.Statement;
import dk.in2isoft.onlineobjects.model.TextHolding;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.caching.CacheEntry;
import dk.in2isoft.onlineobjects.modules.caching.CacheService;
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
	private CacheService cacheService;

	public Question createQuestion(String text, Operator operator) throws ModelException, SecurityException, IllegalRequestException {
		Question question = newQuestion(text);
		modelService.create(question, operator);
		return question;
	}

	public InternetAddress createInternetAddress(String url, User user, Operator operator) throws ModelException, SecurityException, IllegalRequestException, ContentNotFoundException {
		return internetAddressService.create(url, null, user, operator);
	}

	public InternetAddress createInternetAddress(AddressRequest request, Operator operator) throws ModelException, SecurityException, IllegalRequestException, ContentNotFoundException {
		String url = request.getUrl();
		User user = request.getUser();
		Long questionId = request.getQuestionId();
		String title = request.getTitle();
		String quote = request.getQuote();
		InternetAddress internetAddress = internetAddressService.create(url, title, user, operator);

		if (Strings.isNotBlank(quote)) {
			Statement statement = addStatementToInternetAddress(quote, internetAddress, operator);
			if (questionId != null) {
				Question question = modelService.getRequired(Question.class, questionId, operator);
				Optional<Relation> found = modelService.find().relations(operator).from(statement).to(question).withKind(Relation.ANSWERS).first();
				if (!found.isPresent()) {
					modelService.createRelation(statement, question, Relation.ANSWERS, operator);
				}
			}

		}
		return internetAddress;
	}

	public void deleteQuestion(Long id, Operator operator) throws ModelException, ContentNotFoundException, SecurityException {
		Question question = modelService.getRequired(Question.class, id, operator);
		modelService.delete(question, operator);
	}

	public void deleteStatement(Long id, Operator operator) throws ModelException, ContentNotFoundException, SecurityException {
		Statement statement = modelService.getRequired(Statement.class, id, operator);
		modelService.delete(statement, operator);
	}

	public void deleteHypothesis(Long id, Operator operator) throws ModelException, ContentNotFoundException, SecurityException {
		Hypothesis hypothesis = modelService.getRequired(Hypothesis.class, id, operator);
		modelService.delete(hypothesis, operator);
	}

	public void deleteInternetAddress(Long id, Operator operator) throws ModelException, ContentNotFoundException, SecurityException {
		InternetAddress address = modelService.getRequired(InternetAddress.class, id, operator);
		List<Statement> children = modelService.getChildren(address, Relation.KIND_STRUCTURE_CONTAINS, Statement.class, operator);

		modelService.delete(address, operator);

		for (Statement htmlPart : children) {
			modelService.delete(htmlPart, operator);
		}

	}

	public void categorize(Entity entity, CategorizableViewPerspective perspective, User user, Operator operator) throws ModelException, SecurityException, ContentNotFoundException {

		Pile inboxPile = pileService.getOrCreatePileByRelation(user, operator, Relation.KIND_SYSTEM_USER_INBOX);
		Pile favorites = pileService.getOrCreatePileByRelation(user, operator, Relation.KIND_SYSTEM_USER_FAVORITES);
		perspective.setInbox(false);
		boolean favorite = false;
		boolean inbox = false;
		List<Pile> piles = modelService.getParents(entity, null, Pile.class, operator);
		for (Pile pile : piles) {
			if (pile.getId() == inboxPile.getId()) {
				inbox = true;
			} else if (pile.getId() == favorites.getId()) {
				favorite = true;
			}
		}
		perspective.setFavorite(favorite);
		perspective.setInbox(inbox);
	}

	public Statement addStatementToInternetAddress(String text, Long internetAddressId, Operator operator) throws ModelException, ContentNotFoundException, SecurityException, IllegalRequestException {
		InternetAddress address = modelService.getRequired(InternetAddress.class, internetAddressId, operator);
		return addStatementToInternetAddress(text, address, operator);
	}
	
	public Statement addStatementToInternetAddress(String text, InternetAddress address, Operator operator) throws ModelException, ContentNotFoundException, SecurityException, IllegalRequestException {
		if (Strings.isBlank(text)) {
			throw new IllegalRequestException("No text");
		}
		Statement statement = newStatement(text);
		Query<Statement> existingQuery = Query.after(Statement.class).withField("text", statement.getText()).as(operator).from(address, Relation.KIND_STRUCTURE_CONTAINS);
		if (modelService.count(existingQuery, operator) == 0) {
			modelService.create(statement, operator);
			modelService.createRelation(address, statement, Relation.KIND_STRUCTURE_CONTAINS, operator);
			return statement;
		}
		return null;
	}

	public Statement newStatement(String text) throws IllegalRequestException {
		if (Strings.isBlank(text)) {
			throw new IllegalRequestException("A statement must have text");
		}
		Statement statement = new Statement();
		setText(text, statement);
		return statement;
	}

	private void setText(String text, TextHolding statement) throws IllegalRequestException {
		text = text.trim();
		if (text.length() > 10000) {
			throw new IllegalRequestException("The statement is longer than 10000 characters");
		}
		// TODO: Clean multiple spaces etc.
		statement.setName(StringUtils.abbreviate(text, 50));
		statement.setText(text);
	}
	
	public Question newQuestion(String text) throws IllegalRequestException {
		if (Strings.isBlank(text)) {
			throw new IllegalRequestException("A question must have text");
		}
		Question question = new Question();
		setText(text, question);
		return question;
	}

	public Hypothesis newHypothesis(String text) throws IllegalRequestException {
		if (Strings.isBlank(text)) {
			throw new IllegalRequestException("A hypothesis must have text");
		}
		Hypothesis hypothesis = new Hypothesis();
		setText(text, hypothesis);
		return hypothesis;
	}

	public Hypothesis createHypothesis(String text, Operator operator) throws ModelException, SecurityException, IllegalRequestException {
		Hypothesis hypothesis = newHypothesis(text);
		modelService.create(hypothesis, operator);
		return hypothesis;
	}

	public int compareByPosition(Relation a, Relation b) {
		float comp = a.getPosition() - b.getPosition();
		if (comp == 0) {
			return (int) (a.getId() - b.getId());
		}
		return (int)comp;
	}

	public HypothesisApiPerspective getHypothesisPerspective(Long id, User user, Operator operator) throws ModelException, ContentNotFoundException, SecurityException {
		Hypothesis hypothesis = modelService.getRequired(Hypothesis.class, id, operator);
		HypothesisApiPerspective perspective = new HypothesisApiPerspective();
		perspective.setId(hypothesis.getId());
		perspective.setText(hypothesis.getText());

		List<Relation> supports = modelService.find().relations(operator).from(Statement.class).to(hypothesis).withKind(Relation.SUPPORTS).list();
		supports.sort(this::compareByPosition);
		List<StatementApiPerspective> supportsPerspectives = new ArrayList<>();
		for (Relation relation : supports) {
			Statement c = (Statement) relation.getFrom();
			StatementApiPerspective statementPerspective = new StatementApiPerspective();
			statementPerspective.setId(c.getId());
			statementPerspective.setText(c.getText());
			populateStatement(operator, c, statementPerspective);
			supportsPerspectives.add(statementPerspective);
		}
		perspective.setSupporting(supportsPerspectives);
		List<Relation> contradicts = modelService.find().relations(operator).from(Statement.class).to(hypothesis).withKind(Relation.CONTRADTICS).list();
		contradicts.sort((a,b) -> compareByPosition(a,b));
		List<StatementApiPerspective> contradictsPerspectives = new ArrayList<>();
		for (Relation relation : contradicts) {
			Statement c = (Statement) relation.getFrom();
			StatementApiPerspective statementPerspective = new StatementApiPerspective();
			statementPerspective.setId(c.getId());
			statementPerspective.setText(c.getText());
			populateStatement(operator, c, statementPerspective);
			contradictsPerspectives.add(statementPerspective);
		}
		perspective.setContradicting(contradictsPerspectives);
		categorize(hypothesis, perspective, user, operator);
		perspective.setVersion(Versioner.from(hypothesis).and(supports).and(contradicts).get());
		return perspective;
	}

	public StatementApiPerspective getStatementPerspective(Long id, User user, Operator operator) throws ModelException, ContentNotFoundException, SecurityException {
		Statement statement = modelService.getRequired(Statement.class, id, operator);
		StatementApiPerspective perspective = new StatementApiPerspective();
		perspective.setId(statement.getId());
		perspective.setText(statement.getText());
		List<InternetAddress> answers = modelService.getParents(statement, Relation.KIND_STRUCTURE_CONTAINS, InternetAddress.class, operator);
		perspective.setAddresses(answers.stream().map(address -> {
			InternetAddressApiPerspective addressPerspective = new InternetAddressApiPerspective();
			addressPerspective.setId(address.getId());
			addressPerspective.setTitle(address.getName());
			addressPerspective.setUrl(address.getAddress());
			return addressPerspective;
		}).collect(toList()));
		List<Relation> questions = modelService.find().relations(operator).from(statement).to(Question.class).withKind(Relation.ANSWERS).list();
		questions.sort(this::compareByPosition);
		perspective.setQuestions(questions.stream().map(relation -> {
			Question question = (Question) relation.getTo();
			QuestionApiPerspective p = new QuestionApiPerspective();
			p.setId(question.getId());
			p.setText(question.getText());
			return p;
		}).collect(toList()));
		List<Person> authors = modelService.getChildren(statement, Relation.KIND_COMMON_AUTHOR, Person.class, operator);
		List<PersonApiPerspective> authorPerspectives = new ArrayList<>();
		for (Person person : authors) {
			PersonApiPerspective personPerspective = new PersonApiPerspective();
			personPerspective.setName(person.getFullName());
			personPerspective.setId(person.getId());
			authorPerspectives.add(personPerspective);
		}
		perspective.setAuthors(authorPerspectives);
		categorize(statement, perspective, user, operator);
		perspective.setVersion(Versioner.from(statement).and(answers).and(questions).and(authors).get());
		return perspective;
	}
	
	private static class Versioner {
		private long version;
		public static Versioner from(Item item) {
			Versioner v = new Versioner();
			v.version = item.getUpdated().getTime();
			return v;
		}
		
		public <T extends Item> Versioner and(Collection<T> items) {
			for (Item item : items) {
				version = Math.max(version, item.getUpdated().getTime());
				if (item instanceof Relation) {
					Relation relation = (Relation) item;
					version = Math.max(version, relation.getFrom().getUpdated().getTime());
					version = Math.max(version, relation.getTo().getUpdated().getTime());
				}
			}
			return this;
		}
		
		public long get() {
			return version;
		}
	}

	public Statement addPersonalStatement(String text, User user, Operator operator) throws ModelException, SecurityException, IllegalRequestException {
		// TODO Check length
		Statement statement = newStatement(text);
		modelService.create(statement, operator);
		Person person = memberService.getUsersPerson(user, operator);
		if (person != null) {
			modelService.createRelation(statement, person, Relation.KIND_COMMON_AUTHOR, operator);
		}
		return statement;
	}

	public QuestionApiPerspective getQuestionPerspective(Long id, User user, Operator operator) throws ModelException, ContentNotFoundException, SecurityException {
		Question question = modelService.getRequired(Question.class, id, operator);
		QuestionApiPerspective perspective = new QuestionApiPerspective();
		perspective.setId(question.getId());
		perspective.setText(question.getText());

		List<Relation> answers = modelService.find().relations(operator).from(Statement.class).to(question).withKind(Relation.ANSWERS).list();
		answers.sort(this::compareByPosition);
		List<StatementApiPerspective> answerPerspectives = new ArrayList<>();
		for (Relation relation : answers) {
			Statement answer = (Statement) relation.getFrom();
			StatementApiPerspective statementPerspective = new StatementApiPerspective();
			statementPerspective.setId(answer.getId());
			statementPerspective.setText(answer.getText());
			populateStatement(operator, answer, statementPerspective);
			answerPerspectives.add(statementPerspective);
		}
		perspective.setAnswers(answerPerspectives);
		categorize(question, perspective, user, operator);
		perspective.setVersion(Versioner.from(question).and(answers).get());
		return perspective;
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
		statementPerspective.setVersion(Versioner.from(answer).and(authors).and(addresses).get());
	}

	public void updateQuestion(Long id, String text, Boolean inbox, Boolean favorite, User user, Operator operator) throws ModelException, SecurityException, ContentNotFoundException, IllegalRequestException {
		Question question = modelService.getRequired(Question.class, id, operator);
		if (text != null) {
			setText(text, question);
			modelService.update(question, operator);
		}
		if (inbox != null) {
			pileService.changeInboxStatus(question, inbox, user, operator);
		}
		if (favorite != null) {
			pileService.changeFavoriteStatus(question, favorite, user, operator);
		}	
	}

	public void updateStatement(Long id, String text, Boolean inbox, Boolean favorite, User user, Operator operator) throws ModelException, SecurityException, ContentNotFoundException, IllegalRequestException {
		Statement statement = modelService.getRequired(Statement.class, id, operator);
		if (text != null) {
			setText(text, statement);
			modelService.update(statement, operator);
		}
		if (inbox != null) {
			pileService.changeInboxStatus(statement, inbox, user, operator);
		}
		if (favorite != null) {
			pileService.changeFavoriteStatus(statement, favorite, user, operator);
		}	
	}

	public void updateHypothesis(Long id, String text, Boolean inbox, Boolean favorite, User user, Operator operator) throws ModelException, SecurityException, ContentNotFoundException, IllegalRequestException {
		Hypothesis hypothesis = modelService.getRequired(Hypothesis.class, id, operator);
		if (text != null) {
			setText(text, hypothesis);
			modelService.update(hypothesis, operator);
		}
		if (inbox != null) {
			pileService.changeInboxStatus(hypothesis, inbox, user, operator);
		}
		if (favorite != null) {
			pileService.changeFavoriteStatus(hypothesis, favorite, user, operator);
		}					
	}

	public void updateInternetAddress(Long id, String title, Boolean inbox, Boolean favorite, User user, Operator operator) throws ModelException, ContentNotFoundException, SecurityException, IllegalRequestException {
		InternetAddress address = modelService.getRequired(InternetAddress.class, id, operator);
		if (title != null) {
			if (Strings.isBlank(title)) {
				throw new IllegalRequestException("Empty title");
			}
			if (title.length() > 500) {
				throw new IllegalRequestException("Title too long");
			}
			address.setName(title);
			modelService.update(address, operator);
		}
		if (inbox != null) {
			pileService.changeInboxStatus(address, inbox, user, operator);
		}
		if (favorite != null) {
			pileService.changeFavoriteStatus(address, favorite, user, operator);
		}							
	}

	public InternetAddressApiPerspective getAddressPerspective(Long id, Operator operator) throws EndUserException {
		return cacheService.cache(id, operator, InternetAddressApiPerspective.class, () -> {
			InternetAddress address = modelService.getRequired(InternetAddress.class, id, operator);
			User user = modelService.getUser(operator);
			InternetAddressApiPerspective addressPerspective = new InternetAddressApiPerspective();
			addressPerspective.setId(address.getId());
			addressPerspective.setTitle(address.getName()==null ? "" : address.getName());
			addressPerspective.setUrl(address.getAddress());
			Set<Long> ids = new HashSet<>();
			Settings settings = new Settings();
			
			InternetAddressViewPerspective internetAddressViewPerspective = internetAddressViewPerspectiveBuilder.build(address.getId(), settings, user, ids, operator);
			
			addressPerspective.setHtml(internetAddressViewPerspective.getFormatted());
			addressPerspective.setText(internetAddressViewPerspective.getText());
			categorize(address, addressPerspective, user, operator);
			addressPerspective.setStatus("test");
			addressPerspective.setVersion(Versioner.from(address).get());
			return new CacheEntry<>(address.getId(), operator.getIdentity(), ids, addressPerspective);
		});
	}
	
	public QuestionEditPerspective getQuestionEditPerspective(Long id, Operator operator) throws ModelException, ContentNotFoundException {
		@Nullable
		Question statement = modelService.get(Question.class, id, operator);
		if (statement == null) {
			throw new ContentNotFoundException(Question.class, id);
		}
		QuestionEditPerspective perspective = new QuestionEditPerspective();
		perspective.setText(statement.getText());
		perspective.setId(id);
		List<Person> people = getAuthors(statement, operator);
		perspective.setAuthors(buildItemData(people));
		return perspective;
	}

	public HypothesisEditPerspective getHypothesisEditPerspective(Long id, Operator operator) throws ModelException, ContentNotFoundException {
		@Nullable
		Hypothesis hypothesis = modelService.get(Hypothesis.class, id, operator);
		if (hypothesis == null) {
			throw new ContentNotFoundException(Question.class, id);
		}
		HypothesisEditPerspective perspective = new HypothesisEditPerspective();
		perspective.setText(hypothesis.getText());
		perspective.setId(id);
		List<Person> people = getAuthors(hypothesis, operator);
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
		}).collect(toList());
	}

	public List<Person> getAuthors(Entity entity, Operator operator) {
		Query<Person> query = Query.of(Person.class).from(entity, Relation.KIND_COMMON_AUTHOR).as(operator);
		List<Person> people = modelService.list(query, operator);
		return people;
	}

	public ProfileApiPerspective getProfile(User user, Operator operator) throws ModelException {
		ProfileApiPerspective profile = new ProfileApiPerspective();
		profile.setUsername(user.getUsername());
		EmailAddress email = memberService.getUsersPrimaryEmail(user, operator);
		if (email!=null) {
			profile.setEmail(email.getAddress());			
		}
		Person person = memberService.getUsersPerson(user, operator);
		if (person!=null) {
			profile.setFullName(person.getFullName());
		}
		Image photo = memberService.getUsersProfilePhoto(user, operator);
		if (photo!=null) {
			profile.setProfilePhotoId(photo.getId());
		}
		return profile;
	}

	public SearchResult<KnowledgeListRow> search(KnowledgeQuery query, Operator operator) throws ExplodingClusterFuckException, SecurityException, ModelException {
		return readerSearcher.searchOptimized(query, operator);
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
	
	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}

}
