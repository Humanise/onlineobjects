package dk.in2isoft.onlineobjects.apps.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;

import dk.in2isoft.commons.lang.Code;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.parsing.HTMLDocument;
import dk.in2isoft.onlineobjects.apps.knowledge.index.KnowledgeQuery;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Path;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.SearchResult;
import dk.in2isoft.onlineobjects.core.View;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.Error;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Hypothesis;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.Statement;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.images.ImageImporter;
import dk.in2isoft.onlineobjects.modules.importing.DataImporter;
import dk.in2isoft.onlineobjects.modules.knowledge.AddressRequest;
import dk.in2isoft.onlineobjects.modules.knowledge.HypothesisApiPerspective;
import dk.in2isoft.onlineobjects.modules.knowledge.InternetAddressApiPerspective;
import dk.in2isoft.onlineobjects.modules.knowledge.ProfileApiPerspective;
import dk.in2isoft.onlineobjects.modules.knowledge.QuestionApiPerspective;
import dk.in2isoft.onlineobjects.modules.knowledge.StatementApiPerspective;
import dk.in2isoft.onlineobjects.modules.language.TextAnalysis;
import dk.in2isoft.onlineobjects.modules.language.WordModification;
import dk.in2isoft.onlineobjects.modules.user.Agreement;
import dk.in2isoft.onlineobjects.modules.user.ClientInfo;
import dk.in2isoft.onlineobjects.ui.Request;

public class APIController extends APIControllerBase {

	@Path(expression = "/")
	@View(ui = {"web", "front.gui.xml"})
	public void front(Request request) throws IOException {
	}

	@Path(exactly = { "v1.0", "language", "analyse" })
	public TextAnalysis analyse(Request request) throws IOException, EndUserException {
		String text = request.getString("text");
		String url = request.getString("url");
		if (Strings.isNotBlank(url)) {
			text = extractText(url);
		}
		return languageService.analyse(text, request);
	}

	@Path(exactly = { "v1.0", "html", "extract" })
	public void extractText(Request request) throws IOException, EndUserException {
		String url = request.getString("url", "An URL parameters must be provided");
		HttpServletResponse response = request.getResponse();
		response.setCharacterEncoding(Strings.UTF8);
		response.setContentType("text/plain");
		PrintWriter writer = response.getWriter();
		writer.write(extractText(url));
	}
	
	@Path(exactly={"v1.0","signup","check"})
	public MemberCheckResponse checkNewMember(Request request) throws ModelException {
		String username = request.getString("username");
		String password = request.getString("password");
		String email = request.getString("email");
		MemberCheckResponse response = new MemberCheckResponse();
		response.setEmailTaken(memberService.isPrimaryEmailTaken(email, request));
		response.setEmailValid(memberService.isWellFormedEmail(email));
		response.setUsernameTaken(memberService.isUsernameTaken(username, request));
		response.setUsernameValid(memberService.isValidUsername(username));
		response.setPasswordValid(memberService.isValidPassword(password));
		return response;
	}

	@Path(exactly={"v1.0","signup"})
	public AuthenticationResponse signup(Request request) throws IOException, EndUserException {
		String username = request.getString("username");
		String password = request.getString("password");
		String fullName = request.getString("fullName");
		String email = request.getString("email");
		ClientInfo info = getClientInfo(request);
		User user = memberService.signUp(request.getSession(), username, password, fullName, email, request);
		String secret = securityService.getSecret(info, user, request);
		if (Strings.isBlank(secret)) {
			throw new SecurityException("Unable to perform request");
		}

		AuthenticationResponse response = new AuthenticationResponse();
		response.setSecret(secret);
		return response;
	}

	@Path(exactly={"v1.0","changePassword"})
	public void changePassword(Request request) throws IOException, EndUserException {
		String username = request.getString("username", "No username");
		String existingPassword = request.getString("existingPassword", "No existing password");
		String newPassword = request.getString("newPassword", "No new password");
		securityService.changePassword(username, existingPassword, newPassword, request);
	}

	@Path(exactly={"v1.0","recover"})
	public void recover(Request request) throws IOException, EndUserException {
		String usernameOrEmail = request.getString("usernameOrMail", "No username or e-mail provided");
		if (!passwordRecoveryService.sendRecoveryMail(usernameOrEmail, request)) {
			throw new IllegalRequestException("Username or e-mail not found");
		}
	}

	@Path(exactly={"v1.1","recover"})
	public void recoverNew(Request request) throws IOException, EndUserException {
		String usernameOrEmail = request.getString("usernameOrMail", "No username or e-mail provided");
		securityService.randomDelay();
		Boolean success = passwordRecoveryService.sendRecoveryMail(usernameOrEmail, request);
		if (!success) {
			throw new ContentNotFoundException("Username or e-mail not found");
		}
	}

	@Path(exactly={"v1.0","authentication"})
	public AuthenticationResponse authentication(Request request) throws IOException, EndUserException {
		String username = request.getString("username");
		String password = request.getString("password");
		if (Strings.isBlank(username)) {
			surveillanceService.audit().warn("Failed to authenticate because of missing username");
			throw new IllegalRequestException(Error.noUsername);
		}
		if (Strings.isBlank(password)) {
			surveillanceService.audit().warn("Failed to authenticate because of missing password");
			throw new IllegalRequestException(Error.noPassword);
		}
		
		ClientInfo info = getClientInfo(request);
		
		User user = securityService.getUser(username, password, request);
		if (user==null) {
			surveillanceService.audit().warn("Failed to authenticate username={}", username);
			securityService.randomDelay();
			throw new SecurityException("User not found");
		}
		String secret = securityService.getSecret(info, user, request.as(user));
		if (Strings.isBlank(secret)) {
			surveillanceService.audit().warn("Failed to authenticate username={}", username);
			throw new SecurityException("Unable to perform request");
		}

		surveillanceService.audit().info("Authenticated username={}", username);
		AuthenticationResponse response = new AuthenticationResponse();
		response.setSecret(secret);
		return response;
	}

	private ClientInfo getClientInfo(Request request) throws IllegalRequestException {
		ClientInfo info = new ClientInfo();
		info.setUUID(request.getString("id"));
		info.setNickname(request.getString("nickname"));
		info.setHardware(request.getString("hardware"));
		info.setHardwareVersion(request.getString("hardwareVersion"));
		info.setPlatform(request.getString("platform"));
		info.setPlatformVersion(request.getString("platformVersion"));
		info.setClientVersion(request.getString("clientVersion"));
		info.setClientBuild(request.getString("clientBuild"));
		return info;
	}
	
	@Path(exactly={"v1.0","validateClient"})
	public void validateClient(Request request) throws IOException, EndUserException {
		String clientId = request.getString("client");
		// TODO (jm) This makes no sense
		User user = securityService.getUserBySecret(clientId, request);
		if (user==null) {
			throw new SecurityException("User not found");
		}
	}
	
	@Path(exactly={"v1.0","bookmark"})
	@Deprecated
	public void bookmark(Request request) throws IOException, EndUserException {
		checkUser(request);
		String url = request.getString("url", "An URL parameters must be provided");
		String quote = request.getString("quote");

		User user = modelService.getUser(request);
		InternetAddress internetAddress = internetAddressService.create(url, null, user, request);
		if (Strings.isNotBlank(quote)) {
			knowledgeService.addStatementToInternetAddress(quote, internetAddress, request);
		}
	}
	
	@Path(exactly={"v1.0", "image", "add"})
	public void addImage(Request request) throws IOException, EndUserException {
		checkUser(request);

		DataImporter importer = importService.createImporter();
		importer.setListener(new ImageImporter(modelService, imageService) {
			@Override
			protected Privileged getUser(Map<String, String> parameters, Request request) throws EndUserException {
				String secret = parameters.get("secret");
				if (Strings.isBlank(secret)) {
					throw new IllegalRequestException("No secret");
				}
				User user = securityService.getUserBySecret(secret, request);
				if (user == null) {
					throw new SecurityException(Error.userNotFound);
				}
				return user;
			}
			
			@Override
			protected void preProcessImage(Image image, Map<String, String> parameters, Request request) throws EndUserException {
				String tags = parameters.get("tags");
				if (Strings.isNotBlank(tags)) {
					String[] splitted = tags.split(",");
					for (String token : splitted) {
						if (Strings.isNotBlank(token)) {
							image.addProperty(Property.KEY_COMMON_TAG, token.trim());
						}
					}
				}
				String url = parameters.get("url");
				if (Strings.isNotBlank(url)) {
					image.addProperty(Property.KEY_DATA_SOURCE, url);
				}
				String description = parameters.get("description");
				if (Strings.isNotBlank(description)) {
					image.addProperty(Image.PROPERTY_DESCRIPTION, description);
				}
			}
		});
		importer.importMultipart(this, request);
	}
	
	/* ------- Knowledge ------- */
	
	private void checkUser(Request request) throws SecurityException {
		if (securityService.isPublicUser(request)) {
			throw new SecurityException(Error.userNotFound);
		}
	}
	
	@Path(exactly = { "v1.0", "knowledge", "list" })
	public APISearchResult knowledgeList(Request request) throws IOException, EndUserException {
		checkUser(request);
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

		KnowledgeQuery query = new KnowledgeQuery();
		query.setPage(page);
		query.setPageSize(pageSize);
		query.setSubset("everything");
		query.setType(types);
		query.setText(request.getString("text"));
		query.setInbox(request.getBoolean("inbox", null));
		query.setFavorite(request.getBoolean("favorite", null));
		SearchResult<KnowledgeListRow> result = knowledgeService.search(query, request);
		return new APISearchResult(result);
	}

	@Path(exactly = { "v1.0", "knowledge", "question" })
	public QuestionApiPerspective viewQuestion(Request request) throws IOException, EndUserException {
		checkUser(request);
		Long id = request.getId();
		User user = modelService.getUser(request);
		return knowledgeService.getQuestionPerspective(id, user, request);
	}

	@Path(exactly = { "v1.0", "knowledge", "question", "update" })
	public QuestionApiPerspective updateQuestion(Request request) throws IOException, EndUserException {
		checkUser(request);
		User user = modelService.getUser(request);
		Long id = request.getId();
		String text = request.getString("text", "Text is required");
		knowledgeService.updateQuestion(id, text, null, null, user, request);
		return knowledgeService.getQuestionPerspective(id, user, request);
	}

	@Path(exactly = { "v1.1", "knowledge", "question" }, method = "POST")
	public QuestionApiPerspective patchQuestion(Request request) throws IOException, EndUserException {
		checkUser(request);
		User user = modelService.getUser(request);
		Long id = request.getId();
		Boolean inbox = request.getBoolean("inbox", null);
		Boolean favorite = request.getBoolean("favorite", null);
		String text = request.getStringOrNull("text");
		knowledgeService.updateQuestion(id, text, inbox, favorite, user, request);
		return knowledgeService.getQuestionPerspective(id, user, request);
	}

	@Path(exactly = { "v1.0", "knowledge", "question", "add" })
	public QuestionApiPerspective addQuestion(Request request) throws IOException, EndUserException {
		checkUser(request);
		User user = modelService.getUser(request);
		String string = request.getString("text");
		Long statementId = request.getLong("statementId", null);
		Question question = knowledgeService.createQuestion(string, request);
		if (statementId!=null) {
			Statement statement = modelService.getRequired(Statement.class, statementId, request);
			relate(question, statement, request);
		}
		return knowledgeService.getQuestionPerspective(question.getId(), user, request);
	}

	@Path(exactly = { "v1.0", "knowledge", "question", "delete" })
	public void deleteQuestion(Request request) throws IOException, EndUserException {
		checkUser(request);
		Long id = request.getId(); 
		knowledgeService.deleteQuestion(id, request);
	}

	@Path(exactly = { "v1.0", "knowledge", "question", "add", "answer" })
	@Deprecated
	public QuestionApiPerspective addAnswerToQuestion(Request request) throws IOException, EndUserException {
		checkUser(request);
		User user = modelService.getUser(request);
		Question question = modelService.getRequired(Question.class, request.getLong("questionId"), request);
		Statement answer = modelService.getRequired(Statement.class, request.getLong("answerId"), request);
		relate(question, answer, request);
		return knowledgeService.getQuestionPerspective(question.getId(), user, request);
	}

	@Path(exactly = { "v1.0", "knowledge", "question", "add", "statement" })
	public QuestionApiPerspective addStatementToQuestion(Request request) throws IOException, EndUserException {
		checkUser(request);
		User user = modelService.getUser(request);
		Question question = modelService.getRequired(Question.class, request.getLong("questionId"), request);
		Statement statement = modelService.getRequired(Statement.class, request.getLong("statementId"), request);
		relate(question, statement, request);
		return knowledgeService.getQuestionPerspective(question.getId(), user, request);
	}

	@Path(exactly = { "v1.0", "knowledge", "question", "remove", "answer" })
	@Deprecated
	public QuestionApiPerspective removeAnswerFromQuestion(Request request) throws IOException, EndUserException {
		checkUser(request);
		User user = modelService.getUser(request);
		Question question = modelService.getRequired(Question.class, request.getLong("questionId"), request);
		Statement answer = modelService.getRequired(Statement.class, request.getLong("answerId"), request);
		List<Relation> relations = modelService.find().relations(request).from(answer).to(question).withKind(Relation.ANSWERS).list();
		for (Relation relation : relations) {
			modelService.delete(relation, request);
		}
		return knowledgeService.getQuestionPerspective(question.getId(), user, request);
	}

	@Path(exactly = { "v1.0", "knowledge", "question", "remove", "statement" })
	public QuestionApiPerspective removeStatementFromQuestion(Request request) throws IOException, EndUserException {
		checkUser(request);
		User user = modelService.getUser(request);
		Statement statement = modelService.getRequired(Statement.class, request.getLong("statementId"), request);
		Question question = modelService.getRequired(Question.class, request.getLong("questionId"), request);
		List<Relation> relations = modelService.find().relations(request).from(statement).to(question).withKind(Relation.ANSWERS).list();
		for (Relation relation : relations) {
			modelService.delete(relation, request);
		}
		return knowledgeService.getQuestionPerspective(question.getId(), user, request);
	}

	@Path(exactly = { "v1.0", "knowledge", "hypothesis", "remove", "statement" })
	public HypothesisApiPerspective removeStatementFromHypothesis(Request request) throws IOException, EndUserException {
		checkUser(request);
		User user = modelService.getUser(request);
		Hypothesis hypothesis = modelService.getRequired(Hypothesis.class, request.getLong("hypothesisId"), request);
		Statement statement = modelService.getRequired(Statement.class, request.getLong("statementId"), request);
		String kind = getHypothesisRelation(request.getString("relation"));
		List<Relation> relations = modelService.find().relations(request).from(statement).to(hypothesis).withKind(kind).list();
		for (Relation relation : relations) {
			modelService.delete(relation, request);
		}
		return knowledgeService.getHypothesisPerspective(hypothesis.getId(), user, request);
	}

	@Path(exactly = { "v1.0", "knowledge", "statement", "remove", "question" })
	public StatementApiPerspective removeQuestionFromStatement(Request request) throws IOException, EndUserException {
		checkUser(request);
		User user = modelService.getUser(request);
		Question question = modelService.getRequired(Question.class, request.getLong("questionId"), request);
		Statement statement = modelService.getRequired(Statement.class, request.getLong("statementId"), request);
		List<Relation> relations = modelService.find().relations(request).from(statement).to(question).withKind(Relation.ANSWERS).list();
		for (Relation relation : relations) {
			modelService.delete(relation, request);
		}
		return knowledgeService.getStatementPerspective(statement.getId(), user, request);
	}

	@Path(exactly = { "v1.0", "knowledge", "hypothesis" })
	public HypothesisApiPerspective viewHypothesis(Request request) throws IOException, EndUserException {
		checkUser(request);
		User user = modelService.getUser(request);
		Long id = request.getId();
		return knowledgeService.getHypothesisPerspective(id, user, request);
	}

	@Path(exactly = { "v1.0", "knowledge", "hypothesis", "add" })
	public HypothesisApiPerspective addHypothesis(Request request) throws IOException, EndUserException {
		checkUser(request);
		User user = modelService.getUser(request);
		Hypothesis hypothesis = knowledgeService.createHypothesis(request.getString("text"), request);
		return knowledgeService.getHypothesisPerspective(hypothesis.getId(), user, request);
	}

	@Path(exactly = { "v1.0", "knowledge", "hypothesis", "add", "statement" })
	public HypothesisApiPerspective addStatementToHypothesis(Request request) throws IOException, EndUserException {
		checkUser(request);
		User user = modelService.getUser(request);
		String relation = request.getString("relation");
		String kind = getHypothesisRelation(relation);
		Hypothesis hypothesis = modelService.getRequired(Hypothesis.class, request.getLong("hypothesisId"), request);
		Statement statement = modelService.getRequired(Statement.class, request.getLong("statementId"), request);
		relate(hypothesis, kind, statement, request);

		return knowledgeService.getHypothesisPerspective(hypothesis.getId(), user, request);
	}

	private String getHypothesisRelation(String relation) throws IllegalRequestException {
		if (!"supports".equals(relation) && !"contradicts".equals(relation)) {
			throw new IllegalRequestException("Invalid relation: " + relation);
		}
		String kind = "supports".equals(relation) ? Relation.SUPPORTS : Relation.CONTRADTICS;
		return kind;
	}

	private void relate(Hypothesis hypothesis, String kind, Statement statement, Operator operator)
			throws ModelException, SecurityException {
		Optional<Relation> found = modelService.find().relations(operator).from(statement).to(hypothesis).withKind(kind).first();
		if (!found.isPresent()) {
			modelService.createRelation(statement, hypothesis, kind, operator);
		}
	}

	@Path(exactly = { "v1.0", "knowledge", "hypothesis", "delete" })
	public void deleteHypothesis(Request request) throws IOException, EndUserException {
		checkUser(request);
		Long id = request.getId(); 
		knowledgeService.deleteHypothesis(id, request);
	}

	
	@Path(exactly = { "v1.0", "knowledge", "statement" })
	public StatementApiPerspective viewStatement(Request request) throws IOException, EndUserException {
		checkUser(request);
		User user = modelService.getUser(request);
		Long id = request.getId();
		return knowledgeService.getStatementPerspective(id, user, request);
	}

	@Path(exactly = { "v1.0", "knowledge", "statement", "delete" })
	public void deleteStatement(Request request) throws IOException, EndUserException {
		checkUser(request);
		Long id = request.getId(); 
		knowledgeService.deleteStatement(id, request);
	}

	@Path(exactly = { "v1.0", "knowledge", "statement", "update" })
	public StatementApiPerspective updateStatement(Request request) throws IOException, EndUserException {
		checkUser(request);
		User user = modelService.getUser(request);
		Long id = request.getId();
		
		String text = request.getString("text", "Text is required");
		knowledgeService.updateStatement(id, text, null, null, user, request);
		return knowledgeService.getStatementPerspective(id, user, request);
	}

	@Path(exactly = { "v1.1", "knowledge", "statement" }, method="POST")
	public StatementApiPerspective patchStatement(Request request) throws IOException, EndUserException {
		checkUser(request);
		User user = modelService.getUser(request);
		Long id = request.getId();
		Boolean inbox = request.getBoolean("inbox", null);
		Boolean favorite = request.getBoolean("favorite", null);
		String text = request.getStringOrNull("text");
		knowledgeService.updateStatement(id, text, inbox, favorite, user, request);
		return knowledgeService.getStatementPerspective(id, user, request);
	}

	@Path(exactly = { "v1.0", "knowledge", "hypothesis", "update" })
	public HypothesisApiPerspective updateHypothesis(Request request) throws IOException, EndUserException {
		checkUser(request);
		User user = modelService.getUser(request);
		Long id = request.getId();
		String text = request.getString("text", "Text is required");
		knowledgeService.updateHypothesis(id, text, null, null, user, request);
		return knowledgeService.getHypothesisPerspective(id, user, request);
	}

	@Path(exactly = { "v1.1", "knowledge", "hypothesis" }, method = "POST")
	public HypothesisApiPerspective patchHypothesis(Request request) throws IOException, EndUserException {
		checkUser(request);
		User user = modelService.getUser(request);
		Long id = request.getId();
		String text = request.getString("text", "Text is required");
		knowledgeService.updateHypothesis(id, text, null, null, user, request);
		return knowledgeService.getHypothesisPerspective(id, user, request);
	}

	@Path(exactly = { "v1.0", "knowledge", "statement", "add" })
	public StatementApiPerspective addStatement(Request request) throws IOException, EndUserException {
		checkUser(request);
		User user = modelService.getUser(request);
		String text = request.getString("text", "No text provided");
		Long addressId = request.getLong("internetAddressId", null);
		Long hypothesisId = request.getLong("hypothesisId", null);
		Long questionId = request.getLong("questionId", null);
		Statement statement = null;
		if (addressId!=null) {
			statement = knowledgeService.addStatementToInternetAddress(text, addressId, request);
		} else {
			statement = knowledgeService.addPersonalStatement(text, user, request);
		}
		if (hypothesisId!=null) {
			Hypothesis hypothesis = modelService.getRequired(Hypothesis.class, hypothesisId, request);
			String relation = request.getString("hypothesisRelation");
			String kind = getHypothesisRelation(relation);
			relate(hypothesis, kind, statement, request);
		}
		if (questionId!=null) {
			Question question = modelService.getRequired(Question.class, questionId, request);
			relate(question, statement, request);
		}
		if (statement == null) {
			throw new IllegalRequestException("The statement could not be added");
		}
		return knowledgeService.getStatementPerspective(statement.getId(), user, request);
	}

	@Path(exactly = { "v1.0", "knowledge", "statement", "add", "personal" })
	public StatementApiPerspective addPersonalStatement(Request request) throws IOException, EndUserException {
		checkUser(request);
		User user = modelService.getUser(request);
		String text = request.getString("text", "No text provided");
		Statement statement = knowledgeService.addPersonalStatement(text, user, request);
		if (statement == null) {
			throw new IllegalRequestException("The statement could not be added");
		}
		return knowledgeService.getStatementPerspective(statement.getId(), user, request);
	}

	@Path(exactly = { "v1.0", "knowledge", "statement", "add", "question" })
	public StatementApiPerspective addQuestionToStatement(Request request) throws IOException, EndUserException {
		checkUser(request);
		User user = modelService.getUser(request);
		Question question = modelService.getRequired(Question.class, request.getLong("questionId"), request);
		Statement statement = modelService.getRequired(Statement.class, request.getLong("statementId"), request);
		relate(question, statement, request);
		return knowledgeService.getStatementPerspective(statement.getId(), user, request);
	}

	private void relate(Question question, Statement statement, Operator operator) throws ModelException, SecurityException {
		Optional<Relation> found = modelService.find().relations(operator).from(statement).to(question).withKind(Relation.ANSWERS).first();
		if (!found.isPresent()) {
			modelService.createRelation(statement, question, Relation.ANSWERS, operator);
		}
	}

	@Path(exactly = { "v1.0", "knowledge", "profile" })
	public ProfileApiPerspective getProfile(Request request) throws IOException, EndUserException {
		checkUser(request);
		User user = modelService.getUser(request);
		return knowledgeService.getProfile(user, request);
	}

	@Path(exactly = { "v1.0", "knowledge", "internetaddress" })
	public InternetAddressApiPerspective viewAddress(Request request) throws IOException, EndUserException {
		checkUser(request);
		Long id = request.getId();
		return knowledgeService.getAddressPerspective(id, request);
	}	

	@Path(exactly = { "v1.1", "knowledge", "internetaddress" }, method = "POST")
	public InternetAddressApiPerspective patchAddress(Request request) throws IOException, EndUserException {
		checkUser(request);
		User user = modelService.getUser(request);
		Long id = request.getId();
		Boolean inbox = request.getBoolean("inbox", null);
		Boolean favorite = request.getBoolean("favorite", null);
		String title = request.getStringOrNull("title");
		knowledgeService.updateInternetAddress(id, title, inbox, favorite, user, request);
		// TODO: We must commit in order to clear cache used in getAddressPerspective
		// (should be handled in another way)
		request.commit();
		return knowledgeService.getAddressPerspective(id, request);
	}	

	@Path(exactly = { "v1.0", "knowledge", "internetaddress", "add" })
	public InternetAddressApiPerspective addInternetAddress(Request request) throws IOException, EndUserException {
		checkUser(request);
		User user = modelService.getUser(request);
		String url = request.getString("url", "An URL parameters must be provided");

		AddressRequest addressRequest = new AddressRequest();
		addressRequest.setUrl(url);
		addressRequest.setUser(user);
		addressRequest.setQuestionId(request.getLong("questionId", null));
		addressRequest.setTitle(request.getString("title"));
		addressRequest.setQuote(request.getString("quote"));
		
		InternetAddress internetAddress = knowledgeService.createInternetAddress(addressRequest, request);
		return knowledgeService.getAddressPerspective(internetAddress.getId(), request);
	}

	@Path(exactly = { "v1.0", "knowledge", "internetaddress", "delete" })
	public void deleteInternetAddress(Request request) throws IOException, EndUserException {
		checkUser(request);
		Long id = request.getId(); 
		knowledgeService.deleteInternetAddress(id, request);
	}

	@Path(exactly = { "v1.0", "knowledge", "agreements" })
	public List<Agreement> agreements(Request request) throws IOException, EndUserException {
		Locale locale = new Locale(request.getString("locale", "No locale supplied"));
		User user = modelService.getUser(request);
		return memberService.getAgreements(user, locale);
	}	

	@Path(exactly = { "v1.0", "words", "import" })
	public void importWord(Request request) throws IOException, EndUserException {
		if (!securityService.isAdminUser(request)) {
			throw new SecurityException();
		}
		
		WordModification modification = request.getObject("modification", WordModification.class);
		Type listType = new TypeToken<List<WordModification>>() {}.getType();
		List<WordModification> modifications = request.getObject("modifications", listType);
		if (modification!=null) {
			wordService.updateWord(modification , request);
		} else if (Code.isNotEmpty(modifications)) {
			for (WordModification wordModification : modifications) {
				wordService.updateWord(wordModification , request);
			}
		} else {
			throw new IllegalRequestException("No modifications provided");
		}
		
	}

	private String extractText(String url) {
		HTMLDocument doc = htmlService.getDocumentSilently(url);
		if (doc == null) {
			return null;
		}
		return doc.getExtractedText();
	}
}
