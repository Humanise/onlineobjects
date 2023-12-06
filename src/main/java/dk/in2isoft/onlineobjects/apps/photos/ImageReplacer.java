package dk.in2isoft.onlineobjects.apps.photos;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.modules.images.ImageImporter;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.util.images.ImageService;

public class ImageReplacer extends ImageImporter {

	public ImageReplacer(ModelService modelService,ImageService imageService) {
		super(modelService, imageService);
	}
	
	@Override
	protected Image getImage(String fileName, Map<String, String> parameters, Request request)
			throws EndUserException {
		long imageId = Long.parseLong(parameters.get("id"));
		@Nullable
		Image image = modelService.get(Image.class, imageId, request);
		if (image == null) {
			throw new ContentNotFoundException(Image.class, imageId);
		}
		imageService.clearMetaData(image, request);
		return image;
	}
	
	protected void postProcessImage(Image image, Map<String, String> parameters, Request request)
			throws EndUserException {
		imageService.clearCache(image);
	}

	public String getProcessName() {
		return "imageReplacement";
	}
}
