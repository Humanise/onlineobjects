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
import dk.in2isoft.in2igui.FileBasedInterface;
import dk.in2isoft.onlineobjects.apps.knowledge.index.KnowledgeQuery;
import dk.in2isoft.onlineobjects.core.Path;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.SearchResult;
import dk.in2isoft.onlineobjects.core.UserSession;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Hypothesis;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.model.Question;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.Statement;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.images.ImageImporter;
import dk.in2isoft.onlineobjects.modules.importing.DataImporter;
import dk.in2isoft.onlineobjects.modules.knowledge.HypothesisApiPerspective;
import dk.in2isoft.onlineobjects.modules.knowledge.InternetAddressApiPerspective;
import dk.in2isoft.onlineobjects.modules.knowledge.ProfileApiPerspective;
import dk.in2isoft.onlineobjects.modules.knowledge.QuestionApiPerspective;
import dk.in2isoft.onlineobjects.modules.knowledge.StatementApiPerspective;
import dk.in2isoft.onlineobjects.modules.language.WordModification;
import dk.in2isoft.onlineobjects.modules.user.Agreement;
import dk.in2isoft.onlineobjects.modules.user.ClientInfo;
import dk.in2isoft.onlineobjects.service.language.TextAnalysis;
import dk.in2isoft.onlineobjects.ui.Request;

public class APIController extends APIControllerBase {

	@Path(expression = "/")
	public void front(Request request) throws IOException {

		FileBasedInterface ui = new FileBasedInterface(getFile("web", "front.gui.xml"), huiService);
		ui.render(request.getRequest(), request.getResponse());
	}

	@Path(start = { "v1.0", "language", "analyse" })
	public TextAnalysis analyse(Request request) throws IOException, EndUserException {
		String text = request.getString("text");
		String url = request.getString("url");
		if (Strings.isNotBlank(url)) {
			text = extractText(url);
		}
		return languageService.analyse(text);
	}

	@Path(start = { "v1.0", "html", "extract" })
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
		response.setEmailTaken(memberService.isPrimaryEmailTaken(email));
		response.setEmailValid(memberService.isWellFormedEmail(email));
		response.setUsernameTaken(memberService.isUsernameTaken(username));
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
		User user = memberService.signUp(request.getSession(), username, password, fullName, email);
		String secret = securityService.getSecret(info, user);
		if (Strings.isBlank(secret)) {
			throw new SecurityException("Unable to perform request");
		}

		AuthenticationResponse response = new AuthenticationResponse();
		response.setSecret(secret);
		return response;
	}

	@Path(start={"v1.0","changePassword"})
	public void changePassword(Request request) throws IOException, EndUserException {
		String username = request.getString("username", "No username");
		String existingPassword = request.getString("existingPassword", "No existing password");
		String newPassword = request.getString("newPassword", "No new password");
		securityService.changePassword(username, existingPassword, newPassword, request.getSession());
	}

	@Path(start={"v1.0","recover"})
	public void recover(Request request) throws IOException, EndUserException {
		String usernameOrEmail = request.getString("usernameOrMail","No username or e-mail provided");
		if (!passwordRecoveryService.sendRecoveryMail(usernameOrEmail)) {
			throw new IllegalRequestException("Username or e-mail not found");
		}
	}

	@Path(start={"v1.0","authentication"})
	public AuthenticationResponse authentication(Request request) throws IOException, EndUserException {
		String username = request.getString("username");
		String password = request.getString("password");
		
		ClientInfo info = getClientInfo(request);
		
		User user = securityService.getUser(username, password);
		if (user==null) {
			securityService.randomDelay();
			throw new SecurityException("User not found");
		}
		String secret = securityService.getSecret(info, user);
		if (Strings.isBlank(secret)) {
			throw new SecurityException("Unable to perform request");
		}

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
	
	@Path(start={"v1.0","validateClient"})
	public void validateClient(Request request) throws IOException, EndUserException {
		String clientId = request.getString("client");
		// TODO (jm) This makes no sense
		User user = securityService.getUserBySecret(clientId);
		if (user==null) {
			throw new SecurityException("User not found");
		}
	}
	
	@Path(start={"v1.0","bookmark"})
	@Deprecated
	public void bookmark(Request request) throws IOException, EndUserException {
		User user = getUserForSecretKey(request);
		String url = request.getString("url", "An URL parameters must be provided");
		String quote = request.getString("quote");

		InternetAddress internetAddress = internetAddressService.importAddress(url, user);
		if (Strings.isNotBlank(quote)) {
			knowledgeService.addStatementToInternetAddress(quote, internetAddress, user);
		}
	}
	
	/* ------- Knowledge ------- */
	
	@Path(start = { "v1.0", "knowledge", "list" })
	public SearchResult<KnowledgeListRow> knowledgeList(Request request) throws IOException, EndUserException {
		User user = getUserForSecretKey(request);
		
		int page = request.getInt("page");
		int pageSize = request.getInt("pageSize");
		if (pageSize == 0) {
			pageSize = 60;
		}
		request.getStrings("type");
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
		return knowledgeService.search(query, user);
	}

	@Path(exactly = { "v1.0", "knowledge", "question" })
	public QuestionApiPerspective viewQuestion(Request request) throws IOException, EndUserException {
		User user = getUserForSecretKey(request);
		Long id = request.getId();
		return knowledgeService.getQuestionPerspective(id, user);
	}

	@Path(exactly = { "v1.0", "knowledge", "question", "update" })
	public QuestionApiPerspective updateQuestion(Request request) throws IOException, EndUserException {
		User user = getUserForSecretKey(request);
		Long id = request.getId();
		Question dummy = new Question();
		dummy.setId(id);
		dummy.setText(request.getString("text", "Text is required"));
		knowledgeService.updateQuestion(dummy, user);
		modelService.commit();
		return knowledgeService.getQuestionPerspective(id, user);
	}

	@Path(exactly = { "v1.0", "knowledge", "question", "add" })
	public QuestionApiPerspective addQuestion(Request request) throws IOException, EndUserException {
		User user = getUserForSecretKey(request);
		Question question = knowledgeService.createQuestion(request.getString("text"), user);
		return knowledgeService.getQuestionPerspective(question.getId(), user);
	}

	@Path(exactly = { "v1.0", "knowledge", "question", "delete" })
	public void deleteQuestion(Request request) throws IOException, EndUserException {
		User user = getUserForSecretKey(request);
		Long id = request.getId(); 
		knowledgeService.deleteQuestion(id, user);
	}

	@Path(exactly = { "v1.0", "knowledge", "question", "add", "answer" })
	public QuestionApiPerspective addAnswerToQuestion(Request request) throws IOException, EndUserException {
		User user = getUserForSecretKey(request);
		Question question = modelService.getRequired(Question.class, request.getLong("questionId"), user);
		Statement answer = modelService.getRequired(Statement.class, request.getLong("answerId"), user);
		Optional<Relation> found = modelService.find().relations(user).from(answer).to(question).withKind(Relation.ANSWERS).first();
		if (!found.isPresent()) {
			modelService.createRelation(answer, question, Relation.ANSWERS, user);
			modelService.commit();
		}
		return knowledgeService.getQuestionPerspective(question.getId(), user);
	}

	@Path(exactly = { "v1.0", "knowledge", "question", "remove", "answer" })
	public QuestionApiPerspective removeAnswerFromQuestion(Request request) throws IOException, EndUserException {
		User user = getUserForSecretKey(request);
		Question question = modelService.getRequired(Question.class, request.getLong("questionId"), user);
		Statement answer = modelService.getRequired(Statement.class, request.getLong("answerId"), user);
		List<Relation> relations = modelService.find().relations(user).from(answer).to(question).withKind(Relation.ANSWERS).list();
		for (Relation relation : relations) {
			modelService.delete(relation, user);
		}
		modelService.commit();
		return knowledgeService.getQuestionPerspective(question.getId(), user);
	}

	@Path(exactly = { "v1.0", "knowledge", "hypothesis" })
	public HypothesisApiPerspective viewHypothesis(Request request) throws IOException, EndUserException {
		User user = getUserForSecretKey(request);
		Long id = request.getId();
		return knowledgeService.getHypothesisPerspective(id, user);
	}

	@Path(exactly = { "v1.0", "knowledge", "hypothesis", "add" })
	public HypothesisApiPerspective addHypothesis(Request request) throws IOException, EndUserException {
		User user = getUserForSecretKey(request);
		Hypothesis hypothesis = knowledgeService.createHypothesis(request.getString("text"), user);
		return knowledgeService.getHypothesisPerspective(hypothesis.getId(), user);
	}

	@Path(exactly = { "v1.0", "knowledge", "hypothesis", "delete" })
	public void deleteHypothesis(Request request) throws IOException, EndUserException {
		User user = getUserForSecretKey(request);
		Long id = request.getId(); 
		knowledgeService.deleteHypothesis(id, user);
	}

	
	@Path(exactly = { "v1.0", "knowledge", "statement" })
	public StatementApiPerspective viewStatement(Request request) throws IOException, EndUserException {
		User user = getUserForSecretKey(request);
		Long id = request.getId();
		return knowledgeService.getStatementPerspective(id, user);
	}

	@Path(exactly = { "v1.0", "knowledge", "statement", "delete" })
	public void deleteStatement(Request request) throws IOException, EndUserException {
		User user = getUserForSecretKey(request);
		Long id = request.getId(); 
		knowledgeService.deleteStatement(id, user);
	}

	@Path(exactly = { "v1.0", "knowledge", "statement", "update" })
	public StatementApiPerspective updateStatement(Request request) throws IOException, EndUserException {
		User user = getUserForSecretKey(request);
		Long id = request.getId();
		Statement dummy = new Statement();
		dummy.setId(id);
		dummy.setText(request.getString("text", "Text is required"));
		knowledgeService.updateStatement(dummy, user);
		modelService.commit();
		return knowledgeService.getStatementPerspective(id, user);
	}

	@Path(exactly = { "v1.0", "knowledge", "statement", "add" })
	public StatementApiPerspective addStatement(Request request) throws IOException, EndUserException {
		User user = getUserForSecretKey(request);
		Long id = request.getLong("internetAddressId");
		if (id==null) {
			throw new IllegalRequestException("No internet address ID");
		}
		String text = request.getString("text", "No text provided");
		Statement statement = knowledgeService.addStatementToInternetAddress(text, id, user);
		if (statement == null) {
			throw new IllegalRequestException("The statement could not be added");
		}
		return knowledgeService.getStatementPerspective(statement.getId(), user);
	}

	@Path(exactly = { "v1.0", "knowledge", "statement", "add", "personal" })
	public StatementApiPerspective addPersonalStatement(Request request) throws IOException, EndUserException {
		User user = getUserForSecretKey(request);
		String text = request.getString("text", "No text provided");
		Statement statement = knowledgeService.addPersonalStatement(text, user);
		if (statement == null) {
			throw new IllegalRequestException("The statement could not be added");
		}
		return knowledgeService.getStatementPerspective(statement.getId(), user);
	}

	@Path(start = { "v1.0", "knowledge", "profile" })
	public ProfileApiPerspective getProfile(Request request) throws IOException, EndUserException {
		User user = getUserForSecretKey(request);
		return knowledgeService.getProfile(user);
	}

	@Path(exactly = { "v1.0", "knowledge", "internetaddress" })
	public InternetAddressApiPerspective viewAddress(Request request) throws IOException, EndUserException {
		User user = getUserForSecretKey(request);
		Long id = request.getId();
		return knowledgeService.getAddressPerspective(id, new UserSession(user));
	}	

	@Path(exactly = { "v1.0", "knowledge", "internetaddress", "add" })
	public InternetAddressApiPerspective addInternetAddress(Request request) throws IOException, EndUserException {
		User user = getUserForSecretKey(request);
		String url = request.getString("url", "An URL parameters must be provided");
		String quote = request.getString("quote");
		Long questionId = request.getLong("questionId", null);

		InternetAddress internetAddress = knowledgeService.createInternetAddress(url,  quote, questionId, user);
		return knowledgeService.getAddressPerspective(internetAddress, user);
	}

	@Path(exactly = { "v1.0", "knowledge", "internetaddress", "delete" })
	public void deleteInternetAddress(Request request) throws IOException, EndUserException {
		User user = getUserForSecretKey(request);
		Long id = request.getId(); 
		knowledgeService.deleteInternetAddress(id, user);
	}

	@Path(exactly = { "v1.0", "knowledge", "agreements" })
	public List<Agreement> agreements(Request request) throws IOException, EndUserException {
		Locale locale = new Locale(request.getString("locale", "No locale supplied"));
		User user = modelService.getRequired(User.class, request.getSession().getIdentity(), request.getSession());
		return memberService.getAgreements(user, locale);
	}	

	private User getUserForSecretKey(Request request) throws SecurityException {
		String secret = request.getString("secret");
		User user = securityService.getUserBySecret(secret);
		if (user==null) {
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {}
			throw new SecurityException("User not found");
		}
		return user;
	}

	@Path(start = { "v1.0", "words", "import" })
	public void importWord(Request request) throws IOException, EndUserException {
		getUserForSecretKey(request);
		Privileged privileged = securityService.getAdminPrivileged();
		WordModification modification = request.getObject("modification", WordModification.class);
		Type listType = new TypeToken<List<WordModification>>() {}.getType();
		List<WordModification> modifications = request.getObject("modifications", listType);
		if (modification!=null) {
			wordService.updateWord(modification , privileged);
		} else if (Code.isNotEmpty(modifications)) {
			for (WordModification wordModification : modifications) {
				wordService.updateWord(wordModification , privileged);
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
