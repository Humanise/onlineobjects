package dk.in2isoft.onlineobjects.apps.account;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.Path;
import dk.in2isoft.onlineobjects.core.UserSession;
import dk.in2isoft.onlineobjects.core.View;
import dk.in2isoft.onlineobjects.core.exceptions.NotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.Error;
import dk.in2isoft.onlineobjects.core.exceptions.ExplodingClusterFuckException;
import dk.in2isoft.onlineobjects.core.exceptions.BadRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.ui.Blend;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.ui.ScriptWriter;
import dk.in2isoft.onlineobjects.ui.StylesheetWriter;
import dk.in2isoft.onlineobjects.ui.data.Response;


public class AccountController extends AccountControllerBase {
	
	private static final Logger log = LogManager.getLogger(AccountController.class);
	
	public static final String MOUNT = "account";

	@Path(expression = "/(<language>)?")
	@View(jsf = "front.xhtml")
	public void front(Request request) {}

	@Path(expression = "/invitation")
	@View(jsf = "invitation.xhtml")
	public void invitation(Request request) {}

	@Path(expression = "/<language>/password")
	@View(jsf = "password.xhtml")
	public void password(Request request) {}

	@Path(expression = "/<language>/delete")
	@View(jsf = "delete.xhtml")
	public void delete(Request request) {}

	@Path(expression = "/<language>/signup")
	@View(jsf = "signup.xhtml")
	public void signup(Request request) {}

	@Path(expression = "/<language>/welcome")
	@View(jsf = "welcome.xhtml")
	public void welcome(Request request) {}

	@Path(expression = "/<language>/agreements")
	@View(jsf = "agreements.xhtml")
	public void agreements(Request request) {}

	@Path(expression = "/<language>/" + AccountController.EMAIL_CONFIRM_PATH)
	@View(jsf = "confirm.xhtml")
	public void confirm(Request request) {}

	@Path(expression = "/<language>/confirm\\-email\\-change")
	@View(jsf = "confirm-email-change.xhtml")
	public void confirmEmailChange(Request request) throws NotFoundException, BadRequestException, ModelException, SecurityException {
		String key = request.getString("key");
		if ("simulate-success".equals(key)) {
			return;
		}
		memberService.performEmailChangeByKey(key, request);
	}
	
	@Path
	public void changePassword(Request request) throws BadRequestException, SecurityException, ModelException, ExplodingClusterFuckException, NotFoundException {
		User user = getUser(request);
		String username = user.getUsername();
		String currentPassword = request.getString("currentPassword", Error.missingCurrentPassword);
		String newPassword = request.getString("newPassword", Error.missingNewPassword);
		securityService.changePassword(username, currentPassword, newPassword, request);
	}

	@Path
	public void changeName(Request request) throws ModelException, NotFoundException, SecurityException {
		UserSession privileged = request.getSession();
		User user = modelService.getRequired(User.class, privileged.getIdentity(), request);
		Person person = memberService.getUsersPerson(user, request);
		person.setGivenName(request.getString("first"));
		person.setAdditionalName(request.getString("middle"));
		person.setFamilyName(request.getString("last"));
		modelService.update(person, request);
	}

	@Path
	public void confirmEmail(Request request) throws EndUserException {
		User user = getUser(request);
		memberService.sendEmailConfirmation(user, request);
	}

	@Path
	public void changePrimaryEmail(Request request) throws EndUserException {
		String email = request.getString("email", Error.invalidEmail);
		User user = getUser(request);
		memberService.sendEmailChangeRequest(user, email, request);
	}

	private @NonNull User getUser(Request request) throws ModelException, NotFoundException {
		return modelService.getRequired(User.class, request.getIdentity(), request);
	}

	
	@Path
	public void changePasswordUsingKey(Request request) throws BadRequestException, SecurityException, ModelException, ExplodingClusterFuckException {
		String key = request.getString("key", "Key must be provided");
		String password = request.getString("password", "Password must be provided");
		securityService.changePasswordUsingKey(key, password, request);
		securityService.startSession(request);
	}

	@Path
	public void acceptTerms(Request request) throws NotFoundException, ModelException, SecurityException {
		User user = getUser(request);
		memberService.markTermsAcceptance(user, request);
	}

	@Path
	public Response signUp(Request request) {
		Response response = new Response();
		try {
			String code = request.getString("code", "Code is required");
			String username = request.getString("username", "Username is required");
			String email = request.getString("email", "E-mail is required");
			String password = request.getString("password", "Password is required");
			invitationService.signUpFromInvitation(request.getSession(), code, username, email, password, request);
			response.setSuccess(true);
		} catch (EndUserException e) {
			log.warn(e.getMessage(), e);
			response.setDescription(e.getMessage());
		}
		return response;
	}
	
	@Path
	public void deleteAccount(Request request) throws ModelException, SecurityException, BadRequestException {
		String username = request.getString("username", Error.noUsername);
		String password = request.getString("password", Error.noPassword);
		UserSession session = request.getSession();
		User user = modelService.get(User.class, session.getIdentity(), request);
		if (!username.equals(user.getUsername())) {
			throw new BadRequestException(Error.userNotCurrent);
		}
		boolean userChanged = securityService.changeUser(request, username, password, request);
		if (!userChanged) {
			throw new SecurityException(Error.userNotFound);
		}
		memberService.deleteMember(user, request);
		securityService.logOut(session);
	}

	@Path(exactly="status")
	public void status(Request request) throws IOException, ModelException, NotFoundException {
		@NonNull
		User user = getUser(request);
		Map<String,Object> info = new HashMap<>();
		info.put("id", user.getId());
		info.put("username", user.getUsername());
		info.put("displayName", Strings.isNotBlank(user.getName()) ? user.getName() : user.getUsername());
		String origin = request.getRequest().getHeader("Origin");
		if (origin != null) {
			request.getResponse().addHeader("Access-Control-Allow-Origin", origin);
			request.getResponse().addHeader("Access-Control-Allow-Credentials", "true");
		}
		
		request.sendObject(info);
	}

	@Path(exactly="status.js")
	public void statusScript(Request request) throws IOException {
		ScriptWriter writer = new ScriptWriter(request, configurationService);
		Blend blend = new Blend("account_status");
		blend.addPath("apps","account","js","status.js");
		writer.write(blend);
	}

	@Path(exactly="status.css")
	public void statusStyle(Request request) throws IOException {
		StylesheetWriter writer = new StylesheetWriter(request, configurationService);
		Blend blend = new Blend("account_status_css");
		blend.addPath("core","css","oo_footer.css");
		blend.addPath("core","css","oo_link.css");
		blend.addPath("core","css","oo_icon.css");
		blend.addPath("core","css","oo_font.css");
		blend.addPath("core","css","oo_topbar.css");
		writer.write(blend);
	}
}
