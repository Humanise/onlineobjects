package dk.in2isoft.onlineobjects.apps.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;

import dk.in2isoft.commons.lang.Code;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.commons.parsing.HTMLDocument;
import dk.in2isoft.in2igui.FileBasedInterface;
import dk.in2isoft.onlineobjects.apps.reader.index.ReaderQuery;
import dk.in2isoft.onlineobjects.core.Path;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.SearchResult;
import dk.in2isoft.onlineobjects.core.UserSession;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Hypothesis;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.InternetAddress;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.model.Question;
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

	@Path(start={"v1.0","signup"})
	public void signup(Request request) throws IOException, EndUserException {
		String username = request.getString("username", "No username");
		String password = request.getString("password", "No password");
		String fullName = request.getString("fullName");
		String email = request.getString("email");
		memberService.signUp(request.getSession(), username, password, fullName, email);
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
	public ClientKeyResponse getSecret(Request request) throws IOException, EndUserException {
		String username = request.getString("username");
		String password = request.getString("password");
		//String clientId = request.getString("client");
		
		User user = securityService.getUser(username, password);
		if (user==null) {
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {}
			throw new SecurityException("User not found");
		}
		String secret = user.getPropertyValue(Property.KEY_AUTHENTICATION_SECRET);

		ClientKeyResponse response = new ClientKeyResponse();
		response.setSecret(secret);
		return response;
	}
	
	@Path(start={"v1.0","validateClient"})
	public void validateClient(Request request) throws IOException, EndUserException {
		String clientId = request.getString("client");
		
		User user = securityService.getUserBySecret(clientId);
		if (user==null) {
			throw new SecurityException("User not found");
		}
	}
	
	@Path(start={"v1.0","bookmark"})
	public void bookmark(Request request) throws IOException, EndUserException {
		User user = getUserForSecretKey(request);
		String url = request.getString("url", "An URL parameters must be provided");
		String quote = request.getString("quote");

		InternetAddress internetAddress = internetAddressService.importAddress(url, user);
		if (Strings.isNotBlank(quote)) {
			knowledgeService.addStatementToInternetAddress(quote, internetAddress, user);
		}
	}

	@Path(start={"v1.0","addImage"})
	public void addImage(Request request) throws IOException, EndUserException {

		DataImporter importer = importService.createImporter();
		importer.setListener(new ImageImporter(modelService, imageService) {
			@Override
			protected boolean isRequestLegal(Map<String, String> parameters, Request request) throws EndUserException {
				String secret = parameters.get("secret");
				if (Strings.isBlank(secret)) {
					throw new IllegalRequestException("No secret");
				}
				securityService.changeUserBySecret(request.getSession(), secret);
				return true;
			}
			
			@Override
			protected void postProcessImage(Image image, Map<String, String> parameters, Request request) throws EndUserException {
				
			}
		});
		importer.importMultipart(this, request);
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

		ReaderQuery query = new ReaderQuery();
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

	@Path(exactly = { "v1.0", "knowledge", "question", "add" })
	public QuestionApiPerspective addQuestion(Request request) throws IOException, EndUserException {
		User user = getUserForSecretKey(request);
		Question question = knowledgeService.createQuestion(request.getString("text"), user);
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

	@Path(exactly = { "v1.0", "knowledge", "statement" })
	public StatementApiPerspective viewStatement(Request request) throws IOException, EndUserException {
		User user = getUserForSecretKey(request);
		Long id = request.getId();
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

		InternetAddress internetAddress = internetAddressService.importAddress(url, user);

		return knowledgeService.getAddressPerspective(internetAddress, new UserSession(user));
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
