package dk.in2isoft.onlineobjects.apps.account;

import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.in2isoft.onlineobjects.core.Path;
import dk.in2isoft.onlineobjects.core.UserSession;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.ExplodingClusterFuckException;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.ui.data.Response;
import dk.in2isoft.onlineobjects.util.ValidationUtil;


public class AccountController extends AccountControllerBase {
	
	private static final Logger log = LoggerFactory.getLogger(AccountController.class);
	
	public static final String MOUNT = "account";


	@Override
	public boolean isAllowed(Request request) {
		return true;
	}
	
	@Path
	public void changePassword(Request request) throws IllegalRequestException, SecurityException, ModelException, ExplodingClusterFuckException, ContentNotFoundException {
		User user = getUser(request);
		String username = user.getUsername();
		String currentPassword = request.getString("currentPassword", "Current password must be provided");
		String newPassword = request.getString("newPassword", "New password must be provided");
		securityService.changePassword(username, currentPassword, newPassword, request.getSession());
	}

	@Path
	public void changeName(Request request) throws ModelException, ContentNotFoundException, SecurityException {
		UserSession privileged = request.getSession();
		User user = modelService.getRequired(User.class, privileged.getIdentity(), privileged);
		Person person = memberService.getUsersPerson(user, privileged);
		person.setGivenName(request.getString("first"));
		person.setAdditionalName(request.getString("middle"));
		person.setFamilyName(request.getString("last"));
		modelService.updateItem(person, privileged);
	}

	@Path
	public void confirmEmail(Request request) throws EndUserException {
		User user = getUser(request);
		memberService.sendEmailConfirmation(user, request.getSession());
	}

	@Path
	public void changePrimaryEmail(Request request) throws EndUserException {
		String email = request.getString("email", "No email");
		User user = getUser(request);
		memberService.sendEmailChangeRequest(user, email, request.getSession());
	}

	private @NonNull User getUser(Request request) throws ModelException, ContentNotFoundException {
		return modelService.getRequired(User.class, request.getSession().getIdentity(), request.getSession());
	}

	
	@Path
	public void changePasswordUsingKey(Request request) throws IllegalRequestException, SecurityException, ModelException, ExplodingClusterFuckException {
		String key = request.getString("key", "Key must be provided");
		String password = request.getString("password", "Password must be provided");
		securityService.changePasswordUsingKey(key, password, request.getSession());
	}

	@Path
	public void acceptTerms(Request request) throws ContentNotFoundException, ModelException, SecurityException {
		User user = getUser(request);
		memberService.markTermsAcceptance(user, request.getSession());
	}

	@Path
	public Response signUp(Request request) {
		Response response = new Response();
		try {
			String code = request.getString("code", "Code is required");
			String username = request.getString("username", "Username is required");
			String email = request.getString("email", "E-mail is required");
			String password = request.getString("password", "Password is required");
			invitationService.signUpFromInvitation(request.getSession(), code, username, email, password);
			response.setSuccess(true);
		} catch (EndUserException e) {
			log.warn(e.getMessage(), e);
			response.setDescription(e.getMessage());
			modelService.rollBack(); // TODO is this the wrong place to do this?
		}
		return response;
	}
	
	
}
