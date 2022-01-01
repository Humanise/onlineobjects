package dk.in2isoft.onlineobjects.apps.people;

import java.io.IOException;

import dk.in2isoft.onlineobjects.apps.people.utils.ProfileImageImporter;
import dk.in2isoft.onlineobjects.core.Path;
import dk.in2isoft.onlineobjects.core.View;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.images.ImageImporter;
import dk.in2isoft.onlineobjects.modules.importing.DataImporter;
import dk.in2isoft.onlineobjects.modules.user.UserProfileInfo;
import dk.in2isoft.onlineobjects.ui.Request;

public class PeopleController extends PeopleControllerBase {

	public static final String MOUNT = "people";

	@Path(expression = "/(<language>)?")
	@View(jsf = "front.xhtml")
	public void front(Request request) {
	}

	@Path(expression = "/<language>/<username>")
	@View(jsf = "user.xhtml")
	public void user(Request request) {
	}

	@Path
	public UserProfileInfo getUserProfile(Request request) throws EndUserException {
		Long userId = request.getLong("userId");
		User user = modelService.get(User.class, userId, request);
		if (user == null) {
			throw new EndUserException("The user was not found");
		}
		Person person = modelService.getChild(user, Person.class, request);
		if (person == null) {
			throw new EndUserException("The user does not have a person!");
		}
		return memberService.build(person, request);
	}

	@Path
	public void updateUserProfile(Request request) throws EndUserException {
		UserProfileInfo info = request.getObject("info", UserProfileInfo.class);
		User user = modelService.get(User.class, info.getUserId(), request);
		if (user == null) {
			throw new EndUserException("The user was not found");
		}
		Person person = modelService.getChild(user, Person.class, request);
		if (person == null) {
			throw new EndUserException("The user does not have a person!");
		}
		memberService.save(info, person, request);
	}

	@Path
	public void uploadProfileImage(Request request) throws EndUserException, IOException {
		DataImporter dataImporter = importService.createImporter();
		ImageImporter listener = new ProfileImageImporter(modelService, imageService, securityService);
		dataImporter.setListener(listener);
		dataImporter.importMultipart(this, request);
	}
}
