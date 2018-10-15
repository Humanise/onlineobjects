package dk.in2isoft.onlineobjects.service.authentication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.Path;
import dk.in2isoft.onlineobjects.core.UserSession;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.Error;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.service.authentication.perspectives.UserInfoPerspective;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.ui.data.Option;

public class AuthenticationController extends AuthenticationControllerBase {

	private static Logger log = LogManager.getLogger(AuthenticationController.class);

	@Override
	public void unknownRequest(Request request) throws IOException, EndUserException {
		request.redirect("/");
	}

	public void authenticate(Request request) throws IOException, EndUserException {
		String username = request.getString("username");
		String password = request.getString("password");
		String redirect = request.getString("redirect");
		boolean success = securityService.changeUser(request.getSession(), username, password);
		if (success) {
			if (Strings.isNotBlank(redirect)) {
				request.redirectFromBase(redirect);
			} else {
				request.redirect(".?action=loggedIn");
			}
		} else {
			log.warn("Authentication failed");
			request.redirect(".?action=invalidLogin&redirect="+redirect);
		}
	}
	

	public void changeUser(Request request) throws IOException, EndUserException {
		String username = request.getString("username");
		String password = request.getString("password");
		if (!StringUtils.isNotBlank(username)) {
			throw new IllegalRequestException(Error.noUsername);
		}
		if (!StringUtils.isNotBlank(password)) {
			throw new IllegalRequestException(Error.noPassword);
		}
		boolean success = securityService.changeUser(request.getSession(), username, password);
		if (!success) {
			securityService.randomDelay();
			throw new SecurityException(Error.userNotFound);
		}
	}
	

	public void recoverPassword(Request request) throws IOException, EndUserException {
		String usernameOrEmail = request.getString("usernameOrMail","No username or e-mail provided");
		if (passwordRecoveryService.sendRecoveryMail(usernameOrEmail)) {
			
		} else {
			throw new IllegalRequestException("Username or e-mail not found");
		}
	}
	
	@Path(exactly={"signup"})
	public void signup(Request request) throws IOException, EndUserException {
		String username = request.getString("username");
		String password = request.getString("password");
		String fullName = request.getString("fullName");
		String email = request.getString("email");
		memberService.signUp(request.getSession(), username, password, fullName, email);
	}

	public void getUserInfo(Request request) throws ModelException, IOException, IllegalRequestException {
		UserSession session = request.getSession();
		User user = modelService.get(User.class, session.getIdentity(), session);
		if (user==null) {
			throw new IllegalRequestException();
		}
		Image image = memberService.getUsersProfilePhoto(user, session);
		Person person = memberService.getUsersPerson(user, session);
		String language = request.getString("language");
		request.setLanguage(language);
		UserInfoPerspective info = new UserInfoPerspective();
		info.setUsername(user.getUsername());
		if (image!=null) {
			info.setPhotoId(image.getId());
		}
		if (person!=null) {
			info.setFullName(person.getFullName());
		} else {
			info.setFullName(user.getName());
		}
		List<Option> links = new ArrayList<>();
		links.add(Option.of("account", configurationService.getApplicationContext("account", null, request)));
		links.add(Option.of("profile", configurationService.getApplicationContext("people", user.getUsername(), request)));
		info.setLinks(links);
		request.sendObject(info);
	}
	

	public void logout(Request request) throws IOException, EndUserException {
		securityService.logOut(request.getSession());
		request.redirect(".?action=loggedOut");
	}
}
