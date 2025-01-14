package dk.in2isoft.onlineobjects.apps.knowledge;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.annotation.Nullable;
import org.onlineobjects.modules.suggestion.SuggestionsCategory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import dk.in2isoft.commons.lang.Code;
import dk.in2isoft.commons.lang.HTMLWriter;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.in2igui.data.ItemData;
import dk.in2isoft.onlineobjects.apps.api.KnowledgeListRow;
import dk.in2isoft.onlineobjects.apps.knowledge.index.KnowledgeQuery;
import dk.in2isoft.onlineobjects.apps.knowledge.perspective.FeedPerspective;
import dk.in2isoft.onlineobjects.apps.knowledge.perspective.HypothesisEditPerspective;
import dk.in2isoft.onlineobjects.apps.knowledge.perspective.HypothesisViewPerspective;
import dk.in2isoft.onlineobjects.apps.knowledge.perspective.HypothesisWebPerspective;
import dk.in2isoft.onlineobjects.apps.knowledge.perspective.InternetAddressEditPerspective;
import dk.in2isoft.onlineobjects.apps.knowledge.perspective.InternetAddressViewPerspective;
import dk.in2isoft.onlineobjects.apps.knowledge.perspective.InternetAddressViewPerspectiveBuilder.Settings;
import dk.in2isoft.onlineobjects.apps.knowledge.perspective.ListItemPerspective;
import dk.in2isoft.onlineobjects.apps.knowledge.perspective.PeekPerspective;
import dk.in2isoft.onlineobjects.apps.knowledge.perspective.QuestionEditPerspective;
import dk.in2isoft.onlineobjects.apps.knowledge.perspective.QuestionViewPerspective;
import dk.in2isoft.onlineobjects.apps.knowledge.perspective.QuestionWebPerspective;
import dk.in2isoft.onlineobjects.apps.knowledge.perspective.StatementEditPerspective;
import dk.in2isoft.onlineobjects.apps.knowledge.perspective.StatementWebPerspective;
import dk.in2isoft.onlineobjects.core.Ability;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Pair;
import dk.in2isoft.onlineobjects.core.Path;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.SearchResult;
import dk.in2isoft.onlineobjects.core.UserSession;
import dk.in2isoft.onlineobjects.core.View;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.ExplodingClusterFuckException;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.NetworkException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Hypothesis;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.Pile;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.Statement;
import dk.in2isoft.onlineobjects.model.Tag;
import dk.in2isoft.onlineobjects.model.TextHolding;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.model.Word;
import dk.in2isoft.onlineobjects.modules.feeds.Feed;
import dk.in2isoft.onlineobjects.modules.language.TagSelectionQuery;
import dk.in2isoft.onlineobjects.modules.language.WordByInternetAddressQuery;
import dk.in2isoft.onlineobjects.modules.language.WordByMultipleTypesQuery;
import dk.in2isoft.onlineobjects.modules.language.WordListPerspective;
import dk.in2isoft.onlineobjects.modules.language.WordListPerspectiveQuery;
import dk.in2isoft.onlineobjects.modules.networking.NetworkResponse;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.ui.data.Data;
import dk.in2isoft.onlineobjects.ui.data.SimpleEntityPerspective;
import dk.in2isoft.onlineobjects.ui.data.ViewResult;

public class KnowledgeController extends KnowledgeControllerBase {

	private static Logger log = LogManager.getLogger(KnowledgeController.class);


	@Path(expression = "/(da|en)/app/legacy")
	@View(jsf = "reader.xhtml")
	public void legacy(Request request) {}
	
	@Path(expression = "/(da|en)/intro")
	@View(jsf = "intro.xhtml")
	public void intro(Request request) {}
	
	@Path(expression = "/(da|en)/analyze")
	@View(jsf = "analyze.xhtml")
	public void analyze(Request request) {}
	
	@Path(expression = "/(da|en)/extract")
	@View(jsf = "extract.xhtml")
	public void extract(Request request) {}

	@Path(expression = "/(da|en)/app")
	@View(jsf = "app.xhtml")
	public Map<String,String> app2(Request request) {
		Map<String,String> params = new HashedMap<>();
		params.put("domain", request.getBaseDomain());
		return params;
	}
	
	@Path(expression = "/(da|en)/app/ui")
	@View(ui = {"web", "app.xml"})
	public Map<String,String> app(Request request) {
		Map<String,String> params = new HashedMap<>();
		params.put("domain", request.getBaseDomain());
		return params;
	}
	
	@Path(expression = "/app/list")
	public List<KnowledgeListRow> appList(Request request) throws EndUserException, IOException {

		int page = request.getInt("page");
		int pageSize = request.getInt("pageSize");
		if (pageSize == 0) {
			pageSize = 500;
		}
		String type = request.getString("type");
		ArrayList<String> types;
		if (type.equals(Statement.class.getSimpleName())) {
			types = Lists.newArrayList(Statement.class.getSimpleName());
		} else if (type.equals(Question.class.getSimpleName())) {
			types = Lists.newArrayList(Question.class.getSimpleName());
		} else if (type.equals(Hypothesis.class.getSimpleName())) {
			types = Lists.newArrayList(Hypothesis.class.getSimpleName());
		} else if (type.equals(InternetAddress.class.getSimpleName())) {
			types = Lists.newArrayList(InternetAddress.class.getSimpleName());
		} else {
			types = Lists.newArrayList("any");
		}
		String subset = request.getString("subset");

		KnowledgeQuery query = new KnowledgeQuery();
		query.setPage(page);
		query.setPageSize(pageSize);
		query.setSubset("everything");
		query.setType(types);
		query.setText(request.getString("text"));
		query.setWordIds(request.getLongs("words"));
		query.setTagIds(request.getLongs("tags"));
		if ("inbox".equals(subset)) {
			query.setInbox(true);
		}
		if ("archive".equals(subset)) {
			query.setInbox(false);
		}
		if ("favorite".equals(subset)) {
			query.setFavorite(true);
		}
		SearchResult<KnowledgeListRow> result = knowledgeService.search(query, request);

		return result.getList();
	}

	@Path(expression = "/app/delete")
	public void appDeleteInternetAddress(Request request) throws EndUserException, IOException {
		Long id = request.getId();
		String type = request.getString("type");
		if (InternetAddress.class.getSimpleName().equals(type)) {
			modelService.delete(InternetAddress.class, id, request);
		} else if (Statement.class.getSimpleName().equals(type)) {
			modelService.delete(Statement.class, id, request);
		} else if (Question.class.getSimpleName().equals(type)) {
			modelService.delete(Question.class, id, request);
		} else if (Hypothesis.class.getSimpleName().equals(type)) {
			modelService.delete(Hypothesis.class, id, request);
		} else {
			throw new IllegalRequestException("Unknown type");
		}
	}

	@Path(expression = "/app/favorite")
	public void appChangeFavorite(Request request) throws ModelException, SecurityException, IllegalRequestException, ContentNotFoundException {
		Long id = request.getId();
		String type = request.getString("type");
		boolean favorite = request.getBoolean("favorite");

		User user = modelService.getUser(request);		
		Entity entity = loadByType(id, type, request);

		pileService.changeFavoriteStatus(entity, favorite, user, request);
	}

	@Path(expression = "/app/suggest")
	public SuggestionsCategory suggest(Request request) throws IOException, EndUserException {
		if (!request.getSession().has(Ability.earlyAdopter)) {
			return new SuggestionsCategory();
		}
		String text = request.getString("text");
		return knowledgeService.suggestQuestion(text, request);
	}

	@Path(expression = "/app/inbox")
	public void appChangeInbox(Request request) throws ModelException, SecurityException, IllegalRequestException, ContentNotFoundException {
		Long id = request.getId();
		String type = request.getString("type");
		boolean inbox = request.getBoolean("inbox");

		User user = modelService.getUser(request);
		Entity entity = loadByType(id, type, request);

		pileService.changeInboxStatus(entity, inbox, user, request);
	}

	private Entity loadByType(Long id, String type, Operator operator) throws ModelException, ContentNotFoundException, IllegalRequestException {
		Class<? extends Entity> cls = modelService.getEntityClass(type);
		Set<Class<?>> types = Sets.newHashSet(InternetAddress.class, Statement.class, Question.class, Hypothesis.class);
		if (types.contains(cls)) {
			Entity entity = modelService.getRequired(cls, id, operator);
			return entity;			
		}
		throw new IllegalRequestException("Unknown type");
	}
	
	// Statement
	
	@Path(expression = "/app/statement/create")
	public StatementWebPerspective appCreateStatement(Request request) throws IOException, EndUserException {
		String text = request.getString("text");
		Statement statement = knowledgeService.createStatement(text, request);
		return knowledgeService.getStatementWebPerspective(statement.getId(), request);
	}

	@Path(expression = "/app/statement/update")
	public StatementWebPerspective appChangeStatement(Request request) throws EndUserException, IOException {
		User user = modelService.getUser(request);
		Long id = request.getId();
		String text = request.getString("text", "Text is required");
		knowledgeService.updateStatement(id, text, null, null, user, request);
		return knowledgeService.getStatementWebPerspective(id, request);
	}

	@Path(expression = "/app/statement")
	public StatementWebPerspective appStatement(Request request) throws EndUserException, IOException {
		/*try {
			Thread.sleep(Math.round(Math.random()*1000));
		} catch (InterruptedException ignore) {}*/
		Long id = request.getId();
		return knowledgeService.getStatementWebPerspective(id, request);
	}

	@Path(expression = "/app/statement/add/question")
	public StatementWebPerspective appQuestionToStatement(Request request) throws EndUserException, IOException {
		Long questionId = request.getLong("questionId"); 
		Long statementId = request.getLong("statementId"); 
		knowledgeService.addQuestionToStatement(questionId, statementId, request);
		return knowledgeService.getStatementWebPerspective(statementId, request);
	}

	@Path(expression = "/app/statement/remove/question")
	public StatementWebPerspective appRemoveStatementFromQuestion(Request request) throws EndUserException, IOException {
		Long questionId = request.getLong("questionId"); 
		Long statementId = request.getLong("statementId"); 
		knowledgeService.removeQuestionFromStatement(questionId, statementId, request);
		return knowledgeService.getStatementWebPerspective(statementId, request);
	}

	@Path(expression = "/app/statement/suggest")
	public SuggestionsCategory suggestStatement(Request request) throws IOException, EndUserException {
		if (!request.getSession().has(Ability.earlyAdopter)) {
			return new SuggestionsCategory();
		}
		Long id = request.getId();
		Statement statement = modelService.getRequired(Statement.class, id, request);
		return knowledgeService.suggestionsForStatement(statement, request);
	}

	
	// Questions
	
	@Path(expression = "/app/question/create")
	public QuestionWebPerspective appCreateQuestion(Request request) throws IOException, ModelException, SecurityException, IllegalRequestException, ExplodingClusterFuckException, ContentNotFoundException {
		String text = request.getString("text");
		Question question = knowledgeService.createQuestion(text, request);
		return knowledgeService.getQuestionWebPerspective(question.getId(), request);
	}

	@Path(expression = "/app/question/update")
	public QuestionWebPerspective appChangeQuestion(Request request) throws EndUserException, IOException {
		User user = modelService.getUser(request);
		Long id = request.getId();
		String text = request.getString("text", "Text is required");
		knowledgeService.updateQuestion(id, text, null, null, user, request);
		return knowledgeService.getQuestionWebPerspective(id, request);
	}

	@Path(expression = "/app/question")
	public QuestionWebPerspective appQuestion(Request request) throws EndUserException, IOException {
		Long id = request.getId();
		return knowledgeService.getQuestionWebPerspective(id, request);
	}

	@Path(expression = "/app/question/add/statement")
	public QuestionWebPerspective appAddStatementToQuestion(Request request) throws EndUserException, IOException {
		Long questionId = request.getLong("questionId");
		Long statementId = request.getLong("statementId"); 
		knowledgeService.addQuestionToStatement(questionId, statementId, request);
		return knowledgeService.getQuestionWebPerspective(questionId, request);
	}

	@Path(expression = "/app/question/add/answer")
	public QuestionWebPerspective appAddAnswerToQuestion(Request request) throws EndUserException, IOException {
		Long questionId = request.getLong("questionId");
		Long id = request.getLong("answerId");
		String type = request.getString("answerType");
		if (Hypothesis.class.getSimpleName().equalsIgnoreCase(type)) {
			knowledgeService.addQuestionToHypothesis(questionId, id, request);
		} else if (Statement.class.getSimpleName().equalsIgnoreCase(type)) {
			knowledgeService.addQuestionToStatement(questionId, id, request);
		} else {
			throw new IllegalRequestException("Unsupported type: " + type);
		}
		return knowledgeService.getQuestionWebPerspective(questionId, request);
	}
	
	@Path(expression = "/app/question/remove/answer")
	public QuestionWebPerspective appRemoveAnswerFromQuestion(Request request) throws EndUserException, IOException {
		Long questionId = request.getLong("questionId");
		Long id = request.getLong("answerId");
		String type = request.getString("answerType");
		if (Hypothesis.class.getSimpleName().equalsIgnoreCase(type)) {
			knowledgeService.removeQuestionFromHypothesis(questionId, id, request);
		} else if (Statement.class.getSimpleName().equalsIgnoreCase(type)) {
			knowledgeService.removeQuestionFromStatement(questionId, id, request);
		} else {
			throw new IllegalRequestException("Unsupported type: " + type);
		}
		return knowledgeService.getQuestionWebPerspective(questionId, request);
	}

	@Path(expression = "/app/question/remove/statement")
	public QuestionWebPerspective appRemoveQuestionFromAnswer(Request request) throws EndUserException, IOException {
		Long questionId = request.getLong("questionId"); 
		Long statementId = request.getLong("statementId"); 
		knowledgeService.removeQuestionFromStatement(questionId, statementId, request);
		return knowledgeService.getQuestionWebPerspective(questionId, request);
	}

	@Path(expression = "/app/question/remove/hypothesis")
	public QuestionWebPerspective appRemoveHypothesisFromQuestion(Request request) throws EndUserException, IOException {
		Long questionId = request.getLong("questionId"); 
		Long hypothesisId = request.getLong("hypothesisId"); 
		knowledgeService.removeQuestionFromHypothesis(questionId, hypothesisId, request);
		return knowledgeService.getQuestionWebPerspective(questionId, request);
	}


	// Hypothesis
	
	@Path(expression = "/app/hypothesis")
	public HypothesisWebPerspective appHypothesis(Request request) throws EndUserException, IOException {
		Long id = request.getId();
		return knowledgeService.getHypothesisWebPerspective(id, request);
	}
	
	@Path(expression = "/app/hypothesis/create")
	public HypothesisWebPerspective appCreateHypothesis(Request request) throws ModelException, SecurityException, IllegalRequestException, ContentNotFoundException {
		String text = request.getString("text");
		Hypothesis hypothesis = knowledgeService.createHypothesis(text, request);
		return knowledgeService.getHypothesisWebPerspective(hypothesis.getId(), request);
	}

	@Path(expression = "/app/hypothesis/update")
	public HypothesisWebPerspective appChangeHypotheis(Request request) throws EndUserException, IOException {
		User user = modelService.getUser(request);
		Long id = request.getId();
		String text = request.getString("text", "Text is required");
		knowledgeService.updateHypothesis(id, text, null, null, user, request);
		return knowledgeService.getHypothesisWebPerspective(id, request);
	}
	
	@Path(expression = "/app/hypothesis/remove/statement")
	public HypothesisWebPerspective appRemoveStatementFromHypothesis(Request request) throws ModelException, SecurityException, IllegalRequestException, ContentNotFoundException {
		String relation = request.getString("relation");
		Long hypothesisId = request.getLong("hypothesisId"); 
		Long statementId = request.getLong("statementId");
		String kind = getHypothesisRelation(relation);
		knowledgeService.removeStatementFromHypothesis(hypothesisId, kind, statementId, request);
		return knowledgeService.getHypothesisWebPerspective(hypothesisId, request);
	}
	
	@Path(expression = "/app/hypothesis/remove/question")
	public HypothesisWebPerspective appRemoveQuestionFromHypothesis(Request request) throws ModelException, SecurityException, IllegalRequestException, ContentNotFoundException {
		Long hypothesisId = request.getLong("hypothesisId"); 
		Long questionId = request.getLong("questionId");
		knowledgeService.removeQuestionFromHypothesis(questionId, hypothesisId, request);
		return knowledgeService.getHypothesisWebPerspective(hypothesisId, request);
	}
	
	@Path(expression = "/app/hypothesis/add/statement")
	public HypothesisWebPerspective appAddStatementToHypothesis(Request request) throws ModelException, SecurityException, IllegalRequestException, ContentNotFoundException {
		String relation = request.getString("relation");
		Long hypothesisId = request.getLong("hypothesisId"); 
		Long statementId = request.getLong("statementId");
		String kind = getHypothesisRelation(relation);
		knowledgeService.addStatementToHypothesis(hypothesisId, kind, statementId, request);
		return knowledgeService.getHypothesisWebPerspective(hypothesisId, request);
	}
	
	@Path(expression = "/app/hypothesis/add/question")
	public HypothesisWebPerspective appAddQuestionToHypothesis(Request request) throws ModelException, SecurityException, IllegalRequestException, ContentNotFoundException {
		Long hypothesisId = request.getId("hypothesisId"); 
		Long questionId = request.getId("questionId");
		knowledgeService.addQuestionToHypothesis(questionId, hypothesisId, request);
		return knowledgeService.getHypothesisWebPerspective(hypothesisId, request);
	}

	@Path(expression = "/app/relate", method = "POST")
	public void appRelate(Request request) throws ModelException, SecurityException, IllegalRequestException, ContentNotFoundException {
		RelationRequest object = request.getObject(RelationRequest.class);
		Entity from = get(object.from, request);
		Entity to = get(object.to, request);
		if (from instanceof Question && to instanceof Hypothesis && "answers".equals(object.relation)) {
			knowledgeService.addQuestionToHypothesis((Question)from, (Hypothesis) to, request);
		} else {
			throw new IllegalRequestException();
		}
	}

	private Entity get(SimpleEntityPerspective persp, Operator operator) throws ModelException {
		Class<? extends Entity> entityClass = modelService.getEntityClass(persp.getType());
		return modelService.get(entityClass, persp.getId(), operator);
	}

	private String getHypothesisRelation(String relation) throws IllegalRequestException {
		if (relation.equals("contradicts")) return Relation.CONTRADTICS;
		else if (relation.equals("supports")) return Relation.SUPPORTS;
		throw new IllegalRequestException("Unknown relation");
	}


	// Internet address
	
	@Path(expression = "/app/internetaddress")
	public InternetAddressViewPerspective appInternetAddress(Request request) throws EndUserException, IOException {
		Long id = request.getId();
		return knowledgeService.getInternetAddressWebPerspective(id, request);
	}

	@Path(expression = "/app/internetaddress/create")
	public InternetAddressViewPerspective appAddInternetAddress(Request request) throws EndUserException, IOException {

		String url = request.getString("url");
		User user = modelService.getUser(request);
		InternetAddress internetAddress = knowledgeService.createInternetAddress(url, user, request);
		return knowledgeService.getInternetAddressWebPerspective(internetAddress.getId(), request);
	}

	@Path(expression = "/app/internetaddress/create/statement")
	public InternetAddressViewPerspective appCreateStatementOnInternetAddress(Request request) throws EndUserException, IOException {
		Long id = request.getId();
		String text = request.getString("text");
		knowledgeService.addStatementToInternetAddress(text, id, request);
		return knowledgeService.getInternetAddressWebPerspective(id, request);
	}

	@Path(expression = "/app/tags")
	public List<ItemData> tagSelection(Request request) throws ModelException {
		TagSelectionQuery query = new TagSelectionQuery(request);
		/*
		 * List<ItemData> list = modelService.list(query);
		 * Collections.sort(list, (o1, o2) -> { return
		 * Strings.compareCaseless(o1.getText(),o2.getText()); });
		 */
		return modelService.list(query, request);
	}

	@Path(expression = "/app/tag", method = "POST")
	public Object addTag(Request request) throws EndUserException {
		String text = request.getString("text", "A tag must have some text");
		Entity entity = loadByType(request.getId(), request.getString("type"), request);
		text = normaliseTag(text);
		
		Query<Tag> query = Query.after(Tag.class).withNameInAnyCase(text);
		Tag tag = modelService.getFirst(query, request);
		if (tag == null) {
			tag = new Tag();
			tag.setName(text);
			modelService.create(tag, request);
		}
		modelService.ensureRelation(tag, entity, request);
		return getWebPerspective(entity, request);
	}

	private String normaliseTag(String text) {
		if (text == null) {
			text = "";
		}
		return text.trim().replaceAll("[\\s\\n\\r\\t]+", " ");
	}

	@Path(expression = "/app/tag/update", method = "POST")
	public void updateTag(Request request) throws EndUserException {
		String text = request.getString("text", "A tag must have some text");		
		text = normaliseTag(text);
		Tag tag = modelService.getRequired(Tag.class, request.getId(), request);
		tag.setName(text);
		modelService.update(tag, request);
	}

	@Path(expression = "/app/tag/remove", method = "DELETE")
	public Object appRemoveTag(Request request) throws EndUserException {
		Long tagId = request.getId("tagId");
		Entity entity = loadByType(request.getId(), request.getString("type"), request);
		Tag word = modelService.getRequired(Tag.class, tagId, request);
		modelService.find().relations(request).from(word).to(entity).delete(request);
		return getWebPerspective(entity, request);
	}

	@Path(expression = "/app/tag", method = "DELETE")
	public void deleteTag(Request request) throws EndUserException {
		Long tagId = request.getId("id");
		Tag tag = modelService.getRequired(Tag.class, tagId, request);
		modelService.delete(tag, request);
	}

	@Path(expression = "/app/words")
	public List<ItemData> wordSelection(Request request) throws ModelException {
		WordByMultipleTypesQuery query = new WordByMultipleTypesQuery(request);
		/*
		 * List<ItemData> list = modelService.list(query);
		 * Collections.sort(list, (o1, o2) -> { return
		 * Strings.compareCaseless(o1.getText(),o2.getText()); });
		 */
		return modelService.list(query, request);
	}

	@Path(expression = "/app/word", method = "POST")
	public Object appCreateWord(Request request) throws EndUserException {
		Long wordId = request.getId("wordId");
		Entity entity = loadByType(request.getId(), request.getString("type"), request);
		
		Word word = modelService.getRequired(Word.class, wordId, request);
		Optional<Relation> relation = modelService.getRelation(entity, word, request);
		if (!relation.isPresent()) {
			modelService.createRelation(entity, word, request);
		}
		return getWebPerspective(entity, request);
	}

	@Path(expression = "/app/word", method = "DELETE")
	public Object appRemoveWord(Request request) throws EndUserException {
		Long wordId = request.getId("wordId");
		Entity entity = loadByType(request.getId(), request.getString("type"), request);
		Word word = modelService.getRequired(Word.class, wordId, request);
		Optional<Relation> relation = modelService.getRelation(entity, word, request);
		if (relation.isPresent()) {
			modelService.delete(relation.get(), request);
		}
		return getWebPerspective(entity, request);
	}

	private Object getWebPerspective(Entity entity, Request request) throws EndUserException {
		if (entity instanceof Statement) {
			return knowledgeService.getStatementWebPerspective(entity.getId(), request);
		}
		else if (entity instanceof Question) {
			return knowledgeService.getQuestionWebPerspective(entity.getId(), request);
		}
		else if (entity instanceof Hypothesis) {
			return knowledgeService.getHypothesisWebPerspective(entity.getId(), request);
		}
		else if (entity instanceof InternetAddress) {
			return knowledgeService.getInternetAddressWebPerspective(entity.getId(), request);
		}
		return null;
	}
	
	@Path
	public ViewResult list(Request request) throws IOException, ModelException, ExplodingClusterFuckException, SecurityException {

		int page = request.getInt("page");
		int pageSize = request.getInt("pageSize");
		if (pageSize == 0) {
			pageSize = 30;
		}

		SearchResult<Entity> found = readerSearcher.search(request, page, pageSize);

		ViewResult result = new ViewResult();
		result.setTotal(found.getTotalCount());

		List<Entity> entities = found.getList();
		List<ListItemPerspective> list = Lists.newArrayList();
		for (Entity entity : entities) {

			ListItemPerspective perspective = new ListItemPerspective();
			perspective.setTitle(entity.getName());
			perspective.setType(entity.getClass().getSimpleName());
			perspective.setId(entity.getId());

			InternetAddress address = null;

			HTMLWriter writer = new HTMLWriter();
			writer.startDiv().withClass("reader_list_item");
			String title = entity.getName();
			if (Strings.isBlank(title)) {
				title = "-- empty --";
			}
			String url = null;
			UserSession session = request.getSession();
			if (entity instanceof InternetAddress) {
				writer.startH2().withClass("reader_list_title").text(title).endH2();
				perspective.setAddressId(entity.getId());
				address = (InternetAddress) entity;
				perspective.setUrl(address.getAddress());
				perspective.setAddress(Strings.simplifyURL(address.getAddress()));
				url = address.getAddress();
			} else if (entity instanceof Statement) {
				perspective.setStatementId(entity.getId());
				TextHolding htmlPart = (TextHolding) entity;
				writer.startP().withClass("reader_list_text reader_list_quote").text(htmlPart.getText()).endP();
				Query<InternetAddress> query = Query.after(InternetAddress.class).to(entity, Relation.KIND_STRUCTURE_CONTAINS).as(session);
				InternetAddress addr = modelService.search(query, request).getFirst();
				if (addr != null) {
					perspective.setAddressId(addr.getId());
				}
			} else if (entity instanceof Question) {
				perspective.setQuestionId(entity.getId());
				Question question = (Question) entity;
				writer.startP().withClass("reader_list_text reader_list_question").text(question.getText()).endP();
			} else if (entity instanceof Hypothesis) {
				perspective.setHypothesisId(entity.getId());
				Hypothesis question = (Hypothesis) entity;
				writer.startP().withClass("reader_list_text reader_list_hypothesis").text(question.getText()).endP();
			}
			writeSource(entity,url, writer, request);

			List<Word> words = modelService.getChildren(entity, Word.class, request);
			if (Code.isNotEmpty(words)) {
				writer.startP().withClass("reader_list_words");
				List<Pair<Long, String>> tags = Lists.newArrayList();
				for (Iterator<Word> i = words.iterator(); i.hasNext();) {
					Word word = i.next();
					tags.add(Pair.of(word.getId(), word.getText()));
					writer.startA().withClass("reader_list_word js-reader-list-word").withData("id", word.getId()).text(word.getText()).endA();
					if (i.hasNext()) {
						writer.text(" " + Strings.MIDDLE_DOT + " ");
					}
				}
				perspective.setTags(tags);
				writer.endP();
			}
			writer.endDiv();
			perspective.setHtml(writer.toString());
			list.add(perspective);
		}
		result.setItems(list);

		return result;
	}

	private void writeSource(Entity entity, String url, HTMLWriter writer, Operator session) {
		List<Person> authors = knowledgeService.getAuthors(entity, session);
		if (Code.isNotEmpty(authors) || url!=null) {
			boolean first = true;
			writer.startP().withClass("reader_list_source");
			if (url!=null) {
				writer.startA().withClass("reader_list_link js-reader-list-link").withHref(url).text(Strings.getSimplifiedDomain(url)).endA();
				first = false;
			}
			for (Iterator<Person> i = authors.iterator(); i.hasNext();) {
				if (!first) {
					writer.text(" " + Strings.MIDDLE_DOT + " ");
				}
				Person person = (Person) i.next();
				writer.startA().withClass("reader_list_author js-reader-list-author").withDataMap("id",person.getId()).text(person.getFullName()).endA();
				first = false;
			}
			writer.endP();
		}
	}
	
	@Path
	public PeekPerspective peek(Request request) throws ModelException, IllegalRequestException, ContentNotFoundException {
		String type = request.getString("type");
		PeekPerspective perspective = new PeekPerspective();
		HTMLWriter rendering = new HTMLWriter();
		Privileged privileged = request.getSession();
		if ("Link".equals(type)) {
			String url = request.getString("url");
			if (Strings.isBlank(url)) {
				rendering.startH2().text("Empty").endH2();				
			} else {
				Query<InternetAddress> query = Query.after(InternetAddress.class).withField(InternetAddress.FIELD_ADDRESS, url).as(privileged );
				InternetAddress found = modelService.getFirst(query, request);
				perspective.addAction("Open", "open");
				if (found!=null) {
					rendering.startH2().text(found.getName()).endH2();
					rendering.startP().text("Known page").endP();
					perspective.setId(found.getId());
					perspective.setType(InternetAddress.class.getSimpleName());
					perspective.addAction("View", "view");
				} else {
					rendering.startH2().text(Strings.simplifyURL(url)).endH2();
					rendering.startP().text("External web page").endP();
					Data data = new Data().add("url", url);
					perspective.setData(data);
					perspective.setType("Link");
					perspective.addAction("Import", "import");
				}
			}
		} else if (Statement.class.getSimpleName().equals(type)) {
			Long id = request.getId();
			Statement statement = modelService.getRequired(Statement.class, id, request);
			rendering.startH2().text(StringUtils.abbreviate(statement.getText(), 100)).endH2();
			rendering.startP().text(Statement.class.getSimpleName());
			List<Person> authors = knowledgeService.getAuthors(statement, request);
			for (Person person : authors) {
				rendering.text(", ").text(person.getFullName());
			}
			rendering.endP();
			perspective.setId(statement.getId());
			perspective.setType(Statement.class.getSimpleName());
			perspective.addAction("Edit", "edit");
		} else if (Hypothesis.class.getSimpleName().equals(type)) {
			Long id = request.getId();
			Hypothesis hypothesis = modelService.getRequired(Hypothesis.class, id, request);
			rendering.startH2().text(StringUtils.abbreviate(hypothesis.getText(), 100)).endH2();
			rendering.startP().text(Hypothesis.class.getSimpleName());
			List<Person> authors = knowledgeService.getAuthors(hypothesis, request);
			for (Person person : authors) {
				rendering.text(", ").text(person.getFullName());
			}
			rendering.endP();
			perspective.setId(hypothesis.getId());
			perspective.setType(Hypothesis.class.getSimpleName());
			perspective.addAction("Edit", "edit");
		} else if (Word.class.getSimpleName().equals(type)) {
			Long id = request.getId();
			WordListPerspectiveQuery query = new WordListPerspectiveQuery();
			query.withId(id);
			List<WordListPerspective> list = modelService.list(query, request);
			if (!list.isEmpty()) {
				WordListPerspective word = list.get(0);
				rendering.startH2().text(word.getText()).endH2();
				rendering.startP().text("Word: ").text(word.getLanguage()).text(" · ").text(word.getLexicalCategory()).text(" · ").text(word.getGlossary()).endP();
				perspective.setType(Word.class.getSimpleName());
				perspective.setId(word.getId());
				perspective.setData(Data.of("text",word.getText()));
				perspective.addAction("Remove", "remove");
				perspective.addAction("List", "list");
				perspective.addAction("Search", "search");
				perspective.addAction("View", "view");
			}
		} else {
			rendering.startH2().text("Unknown type").endH2();
		}
		perspective.setRendering(rendering.toString());
		return perspective;
	}

	@Path
	public List<FeedPerspective> getFeeds(Request request) throws EndUserException {
		User user = modelService.getUser(request);
		Pile pile = getFeedPile(user, request);
		List<InternetAddress> children = modelService.getChildren(pile, InternetAddress.class, request);
		List<FeedPerspective> options = Lists.newArrayList();
		for (InternetAddress internetAddress : children) {
			options.add(new FeedPerspective(internetAddress.getName(), internetAddress.getId()));
		}
		return options;
	}

	@Path
	public List<ItemData> getTypeOptions(Request request) throws ModelException {
		List<ItemData> options = Lists.newArrayList();
		options.add(new ItemData("any").withText("Any").withIcon("documents_line"));
		options.add(new ItemData(InternetAddress.class.getSimpleName()).withText("Pages").withIcon("document_line"));
		options.add(new ItemData(Statement.class.getSimpleName()).withText("Quotes").withIcon("quote"));
		options.add(new ItemData(Question.class.getSimpleName()).withText("Questions").withIcon("question"));
		options.add(new ItemData(Hypothesis.class.getSimpleName()).withText("Theories").withIcon("hypothesis"));
		return options;
	}

	@Path
	public List<ItemData> getContextOptions(Request request) throws ModelException {
		List<ItemData> options = Lists.newArrayList();
		options.add(new ItemData("everything").withText("Everything").withIcon("documents_line"));
		options.add(new ItemData("inbox").withText("Inbox").withIcon("inbox_line"));
		options.add(new ItemData("archive").withText("Archive").withIcon("archive_line"));
		options.add(new ItemData("favorite").withText("Favorites").withIcon("star_line"));
		return options;
	}

	private Pile getFeedPile(User user, Operator operator) throws ModelException, SecurityException {
		return pileService.getOrCreatePileByKey("feeds", user, operator);
	}

	@Path
	public void addFeed(Request request) throws EndUserException {
		String url = request.getString("url");
		NetworkResponse response = null;
		try {
			response = networkService.get(url);
			if (response.isSuccess()) {
				Feed feed = feedService.parse(response.getFile());
				User user = modelService.getUser(request);
				Pile feeds = getFeedPile(user, request);
				InternetAddress address = new InternetAddress();
				address.setName(feed.getTitle());
				address.setAddress(url);
				modelService.create(address, request);
				modelService.createRelation(feeds, address, request);
			}
		} catch (URISyntaxException e) {
			throw new IllegalRequestException("The URL is not well formed");
		} catch (IOException e) {
			throw new NetworkException("Unable to fetch " + url);
		} finally {
			if (response != null) {
				response.cleanUp();
			}
		}
	}

	@Path
	public void changeStatus(Request request) throws ModelException, SecurityException, IllegalRequestException, ContentNotFoundException {
		Long id = request.getId();
		String type = request.getString("type");
		Boolean favorite = request.getBoolean("favorite",null);
		Boolean inbox = request.getBoolean("inbox",null);
		if (inbox==null && favorite==null) {
			return;
		}

		User user = modelService.getUser(request);
		Entity entity = null;
		@SuppressWarnings("unchecked")
		Set<Class<? extends Entity>> types = Sets.newHashSet(InternetAddress.class, Question.class, Statement.class, Hypothesis.class);
		for (Class<? extends Entity> cls : types) {
			if (cls.getSimpleName().equals(type)) {
				entity = modelService.get(cls, id, request);
			}
		}
		Code.checkNotNull(entity, "Item not found");

		if (favorite!=null) {
			pileService.changeFavoriteStatus(entity, favorite, user, request);
		}
		if (inbox!=null) {
			pileService.changeInboxStatus(entity, inbox, user, request);
		}
	}

	@Path
	public void changeFavoriteStatus(Request request) throws ModelException, SecurityException, IllegalRequestException, ContentNotFoundException {
		Long id = request.getId();
		boolean favorite = request.getBoolean("favorite");

		User user = modelService.getUser(request);

		InternetAddress address = modelService.getRequired(InternetAddress.class, id, request);
		
		pileService.addOrRemoveFromPile(user, Relation.KIND_SYSTEM_USER_FAVORITES, address, favorite, request);
	}

	@Path
	public void changeInboxStatus(Request request) throws ModelException, SecurityException, IllegalRequestException, ContentNotFoundException {
		Long id = request.getId();
		boolean inbox = request.getBoolean("inbox");

		User user = modelService.getUser(request);

		InternetAddress address = modelService.getRequired(InternetAddress.class, id, request);
		pileService.addOrRemoveFromPile(user, Relation.KIND_SYSTEM_USER_INBOX, address, inbox, request);
	}

	@Path
	public InternetAddressViewPerspective loadArticle(Request request) throws IOException, ModelException, SecurityException, IllegalRequestException, ExplodingClusterFuckException,
			ContentNotFoundException {
		Long articleId = request.getId();
		boolean hightlight = request.getBoolean("highlight");
		User user = modelService.getUser(request);
		
		Settings settings = new Settings();
		settings.setHighlight(hightlight);
		settings.setCssNamespace("reader_text_");

		return internetAddressViewPerspectiveBuilder.build(articleId, settings, user, new HashSet<Long>(), request);
	}

	@Path
	public QuestionViewPerspective viewQuestion(Request request) throws EndUserException {
		Long id = request.getId();
		return questionViewPerspectiveBuilder.build(id, request);
	}

	@Path
	public HypothesisViewPerspective viewHypothesis(Request request) throws EndUserException {
		Long id = request.getId();
		return hypothesisViewPerspectiveBuilder.build(id, request);
	}

	@Path
	public void addQuote(Request request) throws EndUserException {
		Long id = request.getId();
		String text = request.getString("text");
		Statement statement = knowledgeService.newStatement(text);
		InternetAddress address = modelService.getRequired(InternetAddress.class, id, request);
		modelService.create(statement, request);
		modelService.createRelation(address, statement, Relation.KIND_STRUCTURE_CONTAINS, request);
	}

	@Path
	public void addHypothesis(Request request) throws IOException, ModelException, SecurityException, IllegalRequestException, ExplodingClusterFuckException, ContentNotFoundException {
		Long id = request.getId();
		String text = request.getString("text");
		Hypothesis hypothesis = knowledgeService.newHypothesis(text);
		InternetAddress address = modelService.getRequired(InternetAddress.class, id, request);
		modelService.create(hypothesis, request);
		modelService.createRelation(address, hypothesis, Relation.KIND_STRUCTURE_CONTAINS, request);
	}

	@Path
	public void addQuestion(Request request) throws IOException, ModelException, SecurityException, IllegalRequestException, ExplodingClusterFuckException, ContentNotFoundException {
		Long id = request.getId();
		String text = request.getString("text");
		Question question = knowledgeService.newQuestion(text);
		InternetAddress address = modelService.getRequired(InternetAddress.class, id, request);
		modelService.create(question, request);
		modelService.createRelation(address, question, Relation.KIND_STRUCTURE_CONTAINS, request);
	}

	@Path
	public void addPerson(Request request) throws IOException, ModelException, SecurityException, IllegalRequestException, ExplodingClusterFuckException, ContentNotFoundException {
		Long id = request.getId();
		String text = request.getString("text");
		InternetAddress address = modelService.getRequired(InternetAddress.class, id, request);
		Person person = personService.getOrCreatePerson(text, request);
		Optional<Relation> relation = modelService.getRelation(address, person, Relation.KIND_COMMON_AUTHOR, request);
		if (!relation.isPresent()) {
			modelService.createRelation(address, person, Relation.KIND_COMMON_AUTHOR, request);				
		}
	}

	@Path
	public SimpleEntityPerspective addInternetAddress(Request request) throws EndUserException {
		String url = request.getString("url");
		if (Strings.isBlank(url)) {
			throw new IllegalRequestException("No URL");
		}
		
		User user = modelService.getUser(request);
		InternetAddress internetAddress = knowledgeService.createInternetAddress(url, user, request);

		return SimpleEntityPerspective.create(internetAddress);
	}

	@Path
	public void removeInternetAddress(Request request) throws ModelException, IllegalRequestException, SecurityException, ContentNotFoundException {
		Long id = request.getId();

		knowledgeService.deleteInternetAddress(id, request);
	}

	@Path
	public void addWord(Request request) throws EndUserException {
		Long internetAddressId = request.getLong("internetAddressId");
		Long wordId = request.getLong("wordId");
		InternetAddress internetAddress = modelService.get(InternetAddress.class, internetAddressId, request);
		Word word = modelService.get(Word.class, wordId, request);
		Optional<Relation> relation = modelService.getRelation(internetAddress, word, request);
		if (!relation.isPresent()) {
			modelService.createRelation(internetAddress, word, request);
		}
	}

	@Path
	public void removeWord(Request request) throws ModelException, SecurityException, ContentNotFoundException {
		Long internetAddressId = request.getLong("internetAddressId");
		Long wordId = request.getLong("wordId");
		InternetAddress internetAddress = modelService.getRequired(InternetAddress.class, internetAddressId, request);
		Word word = modelService.get(Word.class, wordId, request);
		Optional<Relation> relation = modelService.getRelation(internetAddress, word, request);
		if (relation.isPresent()) {
			modelService.delete(relation.get(), request);
		}
	}

	@Path
	public void removeTag(Request request) throws ModelException, SecurityException, IllegalRequestException, ContentNotFoundException {
		Long internetAddressId = request.getLong("internetAddressId");
		String tag = request.getString("tag");
		InternetAddress internetAddress = modelService.getRequired(InternetAddress.class, internetAddressId, request);
		Collection<Property> properties = internetAddress.getProperties();
		for (Iterator<Property> i = properties.iterator(); i.hasNext();) {
			Property property = i.next();
			if (Property.KEY_COMMON_TAG.equals(property.getKey()) && tag.equals(property.getValue())) {
				i.remove();
			}
		}
		modelService.update(internetAddress, request);
	}

	@Path
	public List<ItemData> getWordCloud(Request request) throws ModelException {
		WordByInternetAddressQuery query = new WordByInternetAddressQuery(request);
		/*
		 * List<ItemData> list = modelService.list(query);
		 * Collections.sort(list, (o1, o2) -> { return
		 * Strings.compareCaseless(o1.getText(),o2.getText()); });
		 */
		return modelService.list(query, request);
	}

	@Path
	public void reIndex(Request request) throws EndUserException {
		readerIndexer.reIndex(request);
	}

	@Path
	public StatementEditPerspective loadStatement(Request request) throws ModelException, IllegalRequestException, ContentNotFoundException {
		Long id = request.getId();
		UserSession session = request.getSession();
		Statement statement = modelService.getRequired(Statement.class, id, request);
		StatementEditPerspective perspective = new StatementEditPerspective();
		perspective.setText(statement.getText());
		perspective.setId(id);
		{
			List<Person> people = knowledgeService.getAuthors(statement, request);
			perspective.setAuthors(people.stream().map((Person p) -> {
				ItemData option = new ItemData();
				option.setId(p.getId());
				option.setText(p.getFullName());
				option.setIcon(p.getIcon());
				return option;
			}).collect(Collectors.toList()));
		}
		Query<Question> query = Query.after(Question.class).from(statement, Relation.ANSWERS).as(session);
		List<Question> questions = modelService.list(query, request);
		perspective.setQuestions(questions.stream().map((Question q) -> {
			ItemData option = new ItemData();
			option.setId(q.getId());
			option.setText(q.getText());
			option.setIcon(q.getIcon());
			return option;
		}).collect(Collectors.toList()));

		Function<Hypothesis, ? extends ItemData> hypothesisMapper = (Hypothesis q) -> {
			ItemData option = new ItemData();
			option.setId(q.getId());
			option.setText(q.getText());
			option.setIcon(q.getIcon());
			return option;
		};

		Query<Hypothesis> supportsQuery = Query.after(Hypothesis.class).from(statement, Relation.SUPPORTS).as(session);
		List<Hypothesis> supports = modelService.list(supportsQuery, request);
		perspective.setSupports(supports.stream().map(hypothesisMapper).collect(Collectors.toList()));

		Query<Hypothesis> contraQuery = Query.after(Hypothesis.class).from(statement, Relation.CONTRADTICS).as(session);
		List<Hypothesis> contradicts = modelService.list(contraQuery, request);
		perspective.setContradicts(contradicts.stream().map(hypothesisMapper).collect(Collectors.toList()));

		return perspective;
	}

	@Path
	public void saveAddress(Request request) throws ModelException, IllegalRequestException, SecurityException {
		InternetAddressEditPerspective perspective = request.getObject("data", InternetAddressEditPerspective.class);
		InternetAddressEditPerspective.validate(perspective);
		InternetAddress internetAddress = modelService.get(InternetAddress.class, perspective.getId(), request);
		if (internetAddress == null) {
			throw new IllegalRequestException("The address was not found");
		}
		internetAddress.setName(perspective.getTitle());
		boolean addressChanged = !Strings.equals(perspective.getAddress(), internetAddress.getAddress());
		internetAddress.setAddress(perspective.getAddress());

		modelService.update(internetAddress, request);

		Collection<Long> ids = ItemData.getIds(perspective.getAuthors());
		modelService.syncRelationsFrom(internetAddress, Relation.KIND_COMMON_AUTHOR, Person.class, ids, request);

		if (addressChanged) {
			File itemFolder = storageService.getItemFolder(internetAddress.getId());
			File original = new File(itemFolder, "original");
			if (original.exists() && !original.delete()) {
				log.error("Unable to delete original for internetAddress=" + internetAddress.getId());
			}
		}

	}

	@Path
	public void saveStatement(Request request) throws ModelException, IllegalRequestException, SecurityException {
		StatementEditPerspective perspective = request.getObject("data", StatementEditPerspective.class);
		long id = perspective.getId();
		if (id < 1) {
			throw new IllegalRequestException("No id");
		}
		String text = perspective.getText();
		if (Strings.isBlank(text)) {
			throw new IllegalRequestException("The text is empty");
		}
		Statement statement = modelService.get(Statement.class, id, request);
		statement.setName(StringUtils.abbreviate(text, 50));
		statement.setText(text);

		modelService.update(statement, request);
		Collection<Long> ids = ItemData.getIds(perspective.getAuthors());
		modelService.syncRelationsFrom(statement, Relation.KIND_COMMON_AUTHOR, Person.class, ids, request);

		Collection<Long> questionIds = ItemData.getIds(perspective.getQuestions());
		modelService.syncRelationsFrom(statement, Relation.ANSWERS, Question.class, questionIds, request);

		Collection<Long> supportsIds = ItemData.getIds(perspective.getSupports());
		modelService.syncRelationsFrom(statement, Relation.SUPPORTS, Hypothesis.class, supportsIds, request);

		Collection<Long> contradictsIds = ItemData.getIds(perspective.getContradicts());
		modelService.syncRelationsFrom(statement, Relation.CONTRADTICS, Hypothesis.class, contradictsIds, request);
	}

	@Path
	public void deleteStatement(Request request) throws IllegalRequestException, ModelException, SecurityException {
		Long id = request.getLong("id");
		if (id == null) {
			throw new IllegalRequestException("No id");
		}
		@Nullable
		Statement statement = modelService.get(Statement.class, id, request);
		if (statement == null) {
			throw new IllegalRequestException("Statement not found");
		}
		modelService.delete(statement, request);
	}

	@Path
	public QuestionEditPerspective editQuestion(Request request) throws ModelException, IllegalRequestException, ContentNotFoundException {
		Long id = request.getId();
		return knowledgeService.getQuestionEditPerspective(id, request);
	}

	@Path
	public void saveQuestion(Request request) throws ModelException, IllegalRequestException, SecurityException, ContentNotFoundException {
		QuestionEditPerspective perspective = request.getObject("data", QuestionEditPerspective.class);
		long id = perspective.getId();
		if (id < 1) {
			throw new IllegalRequestException("No id");
		}
		String text = perspective.getText();
		if (Strings.isBlank(text)) {
			throw new IllegalRequestException("The text is empty");
		}
		Question question = modelService.getRequired(Question.class, id, request);
		question.setName(StringUtils.abbreviate(text, 50));
		question.setText(text);

		modelService.update(question, request);
		Collection<Long> ids = ItemData.getIds(perspective.getAuthors());
		modelService.syncRelationsFrom(question, Relation.KIND_COMMON_AUTHOR, Person.class, ids, request);
	}

	@Path
	public void deleteQuestion(Request request) throws IllegalRequestException, ModelException, SecurityException, ContentNotFoundException {
		Long id = request.getId();
		Question question = modelService.getRequired(Question.class, id, request);
		modelService.delete(question, request);
	}

	@Path
	public HypothesisEditPerspective editHypothesis(Request request) throws ModelException, IllegalRequestException, ContentNotFoundException {
		Long id = request.getId();
		return knowledgeService.getHypothesisEditPerspective(id, request);
	}

	@Path
	public void saveHypothesis(Request request) throws ModelException, IllegalRequestException, SecurityException, ContentNotFoundException {
		HypothesisEditPerspective perspective = request.getObject("data", HypothesisEditPerspective.class);
		long id = perspective.getId();
		if (id < 1) {
			throw new IllegalRequestException("No id");
		}
		String text = perspective.getText();
		if (Strings.isBlank(text)) {
			throw new IllegalRequestException("The text is empty");
		}
		Hypothesis hypothesis = modelService.getRequired(Hypothesis.class, id, request);
		hypothesis.setName(StringUtils.abbreviate(text, 50));
		hypothesis.setText(text);

		modelService.update(hypothesis, request);
		Collection<Long> ids = ItemData.getIds(perspective.getAuthors());
		modelService.syncRelationsFrom(hypothesis, Relation.KIND_COMMON_AUTHOR, Person.class, ids, request);
	}

	@Path
	public void deleteHypothesis(Request request) throws IllegalRequestException, ModelException, SecurityException, ContentNotFoundException {
		Long id = request.getId();
		Hypothesis question = modelService.getRequired(Hypothesis.class, id, request);
		modelService.delete(question, request);
	}
}
