package dk.in2isoft.onlineobjects.apps.people.utils;

import java.util.List;
import java.util.Map;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.images.ImageImporter;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.util.images.ImageService;

public class ProfileImageImporter extends ImageImporter {

	private SecurityService securityService;

	public ProfileImageImporter(ModelService modelService, ImageService imageService, SecurityService securityService) {
		super(modelService, imageService);
		this.securityService = securityService;
	}

	@Override
	protected void postProcessImage(Image image, Map<String,String> parameters, Request request) throws EndUserException {

		User user = request.getSession().getUser();
		securityService.makePublicVisible(image, request.getSession());
		List<Relation> list = modelService.getRelationsFrom(user, Image.class, Relation.KIND_SYSTEM_USER_IMAGE, user);
		for (Relation relation : list) {
			modelService.deleteRelation(relation, this.securityService.getAdminPrivileged());
		}
		modelService.createRelation(user, image, Relation.KIND_SYSTEM_USER_IMAGE, request.getSession());
	}
}