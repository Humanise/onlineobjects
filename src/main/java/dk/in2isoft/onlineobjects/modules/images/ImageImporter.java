package dk.in2isoft.onlineobjects.modules.images;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.modules.importing.ImportListener;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.util.images.ImageService;

public class ImageImporter implements ImportListener<Object> {

	protected ModelService modelService;
	private ImageService imageService;
	private List<Image> importedImages;

	public ImageImporter(ModelService modelService, ImageService imageService) {
		super();
		this.modelService = modelService;
		this.imageService = imageService;
		importedImages = new ArrayList<Image>();
	}

	public final void processFile(File file, String mimeType, String name, Map<String, String> parameters, Request request) throws IOException, EndUserException {
		if (!isRequestLegal(parameters,request)) {
			throw new IllegalRequestException("The request is illegal!");
		}
		if (!imageService.isSupportedMimeType(mimeType)) {
			throw new IllegalRequestException("Unsupported mime type: "+mimeType);
		}
		Image image = new Image();
		Operator privileged = request.as(getUser(parameters, request));
		modelService.create(image, request);
		image.setName(name);
		imageService.changeImageFile(image, file, mimeType);
		imageService.synchronizeMetaData(image, privileged);
		preProcessImage(image, parameters, request);
		modelService.update(image, privileged);
		importedImages.add(image);
		image = modelService.get(Image.class, image.getId(), privileged);
		postProcessImage(image, parameters, request);
		privileged.commit();
	}
	
	protected void preProcessImage(Image image, Map<String, String> parameters, Request request) throws EndUserException {
		
	}

	protected Privileged getUser(Map<String, String> parameters, Request request) throws EndUserException {
		return request.getSession();
	}
	
	protected boolean isRequestLegal(Map<String, String> parameters, Request request) throws EndUserException {
		return true;
	}

	protected void postProcessImage(Image image, Map<String, String> parameters, Request request) throws EndUserException {
		// Override this
	}
	
	@Override
	public Object getResponse() {
		return null;
	}

	public String getProcessName() {
		return "imageImport";
	}

	public List<Image> getImportedImages() {
		return importedImages;
	}
}