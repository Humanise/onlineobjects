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
import dk.in2isoft.onlineobjects.core.View;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.Error;
import dk.in2isoft.onlineobjects.core.exceptions.BadRequestException;
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


	@Path(expression = "/")
	@View(jsf = "login.xhtml")
	public void login(Request request) {
		
	}
	
	@Override
	public String getLanguage(Request request) {
		String lang = request.getRequest().getLocale().getLanguage();
		if (!"da".equals(lang) && !"en".equals(lang)) {
			lang = "en";
		}
		return lang;
	}

	@Override
	public void unknownRequest(Request request) throws IOException, EndUserException {
		request.redirect("/");
	}

	@Path
	public void authenticate(Request request) throws IOException, EndUserException {
		String username = request.getString("username");
		String password = request.getString("password");
		String redirect = request.getString("redirect");
		boolean success = securityService.changeUser(request, username, password, request);
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
	

	@Path
	public void changeUser(Request request) throws IOException, EndUserException {
		String username = request.getString("username");
		String password = request.getString("password");
		if (!StringUtils.isNotBlank(username)) {
			throw new BadRequestException(Error.noUsername);
		}
		username = username.toLowerCase();
		if (!StringUtils.isNotBlank(password)) {
			throw new BadRequestException(Error.noPassword);
		}
		boolean success = securityService.changeUser(request, username, password, request);
		if (!success) {
			securityService.randomDelay();
			throw new SecurityException(Error.userNotFound);
		} else {
			securityService.startSession(request);
		}
	}
	

	@Path
	public void recoverPassword(Request request) throws IOException, EndUserException {
		String usernameOrEmail = request.getString("usernameOrMail","No username or e-mail provided");
		if (passwordRecoveryService.sendRecoveryMail(usernameOrEmail, request)) {
			
		} else {
			throw new BadRequestException("Username or e-mail not found");
		}
	}
	
	@Path(exactly={"signup"})
	public void signup(Request request) throws IOException, EndUserException {
		String username = request.getString("username");
		String password = request.getString("password");
		String fullName = request.getString("fullName");
		String email = request.getString("email");
		memberService.signUp(request.getSession(), username, password, fullName, email, request);
		securityService.startSession(request);
	}

	@Path
	public void getUserInfo(Request request) throws ModelException, IOException, BadRequestException {
		UserSession session = request.getSession();
		User user = modelService.get(User.class, session.getIdentity(), request);
		if (user==null) {
			throw new BadRequestException();
		}
		Image image = memberService.getUsersProfilePhoto(user, request);
		Person person = memberService.getUsersPerson(user, request);
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
		if (securityService.isPublicView(user, request)) {
			links.add(Option.of("profile", configurationService.getApplicationContext("people", user.getUsername(), request)));
		}
		info.setLinks(links);
		request.sendObject(info);
	}
	

	@Path
	public void logout(Request request) throws IOException, EndUserException {
		securityService.logOut(request.getSession());
		request.getRequest().getSession().invalidate();
		request.clearCookies();
		if (request.acceptsHtml()) {
			String redirect = request.getString("redirect");
			String url = ".?action=loggedOut";
			if (Strings.isNotBlank(redirect)) {
				url += "&redirect=" + redirect;
			}
	 		request.redirect(url);
		}
	}
}
