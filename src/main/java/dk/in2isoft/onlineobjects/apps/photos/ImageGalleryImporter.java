package dk.in2isoft.onlineobjects.apps.photos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.ImageGallery;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.modules.images.ImageImporter;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.ui.data.SimpleEntityPerspective;
import dk.in2isoft.onlineobjects.util.images.ImageService;

public class ImageGalleryImporter extends ImageImporter {

	private List<SimpleEntityPerspective> imported;
	private SecurityService security;

	public ImageGalleryImporter(ModelService modelService, ImageService imageService, SecurityService securityService) {
		super(modelService, imageService);
		this.security = securityService;
		imported = new ArrayList<SimpleEntityPerspective>();
	}

	@Override
	protected void postProcessImage(Image image, Map<String,String> parameters, Request request) throws EndUserException {

		int index = Integer.parseInt(parameters.get("index"));
		long imageGalleryId = Long.parseLong(parameters.get("galleryId"));
		ImageGallery gallery = modelService.getRequired(ImageGallery.class, imageGalleryId, request);
		Relation relation = new Relation(gallery, image);
		relation.setPosition(getMaxImagePosition(gallery, request) + 1 + index);
		modelService.create(relation, request);
		if (security.isPublicView(gallery, request)) {
			security.makePublicVisible(image, request);
			security.makePublicVisible(relation, request);
		}

		imported.add(SimpleEntityPerspective.create(image));
	}

	private float getMaxImagePosition(Entity gallery, Operator privileged) throws EndUserException {
		float max = 0;
		List<Relation> relations = modelService.getRelationsFrom(gallery,Image.class, privileged);
		for (Relation relation : relations) {
			max = Math.max(max, relation.getPosition());
		}
		return max;
	}

	@Override
	public Object getResponse() {
		return imported;
	}
}
