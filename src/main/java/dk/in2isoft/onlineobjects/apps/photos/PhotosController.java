package dk.in2isoft.onlineobjects.apps.photos;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import dk.in2isoft.in2igui.data.ListData;
import dk.in2isoft.onlineobjects.apps.photos.perspectives.GalleryModificationRequest;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Path;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.SearchResult;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.IllegalRequestException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.ImageGallery;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.Word;
import dk.in2isoft.onlineobjects.modules.importing.DataImporter;
import dk.in2isoft.onlineobjects.modules.importing.ImportListener;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.ui.data.SimpleEntityPerspective;
import dk.in2isoft.onlineobjects.util.images.ImageInfo.ImageLocation;
import dk.in2isoft.onlineobjects.util.images.ImageMetaData;


public class PhotosController extends PhotosControllerBase {

	@Path(exactly="updateTitle")
	public void updateImageTitle(Request request) throws ModelException, SecurityException, ContentNotFoundException {
		long id = request.getInt("id");
		String title = request.getString("title");
		Image image = getImage(id, request);
		image.setName(title);
		modelService.update(image, request);
	}

	@Path(exactly="updateDescription")
	public void updateImageDescription(Request request) throws ModelException, SecurityException, ContentNotFoundException {
		long id = request.getInt("id");
		String description = request.getString("description");
		Image image = getImage(id,request);
		image.overrideFirstProperty(Image.PROPERTY_DESCRIPTION, description);
		modelService.update(image, request);
	}

	@Path(exactly="updateLocation")
	public void updateImageLocation(Request request) throws ModelException, SecurityException, ContentNotFoundException {
		long id = request.getInt("id");
		ImageLocation location = request.getObject("location", ImageLocation.class);
		Image image = getImage(id, request);
		imageService.updateImageLocation(image, location, request);
	}

	@Path(exactly="relateWord")
	public void relateWordToImage(Request request) throws ModelException, SecurityException, ContentNotFoundException {
		long imageId = request.getInt("image");
		long wordId = request.getInt("word");
		Image image = getImage(imageId, request);
		Word word = modelService.getRequired(Word.class, wordId, request);
		Optional<Relation> relation = modelService.getRelation(image, word, request);
		if (!relation.isPresent()) {
			modelService.createRelation(image, word, request);
		}
	}

	@Path(exactly="removeWord")
	public void removeWordFromImage(Request request) throws ModelException, SecurityException, ContentNotFoundException {
		long imageId = request.getInt("image");
		long wordId = request.getInt("word");
		Image image = getImage(imageId, request);
		Word word = modelService.getRequired(Word.class, wordId, request);
		Optional<Relation> relation = modelService.getRelation(image, word, request);
		if (relation.isPresent()) {
			modelService.delete(relation.get(), request);
		}
	}

	@Path
	public void searchWords(Request request) throws IOException {
		String text = request.getString("text");
		int page = request.getInt("page");
		ListData list = new ListData();
		list.addHeader("Word");
		Query<Word> query = Query.of(Word.class).withWords(text).withPaging(page, 50);
		SearchResult<Word> result = modelService.search(query, request);
		list.setWindow(result.getTotalCount(), 50, page);
		for (Word word : result.getList()) {
			String kind = word.getClass().getSimpleName().toLowerCase();
			list.newRow(word.getId(),kind);
			list.addCell(word.getName(), word.getIcon());
		}
		request.sendObject(list);
	}
	
	@Path
	public void deleteImage(Request request) throws SecurityException, ModelException, ContentNotFoundException {
		long imageId = request.getLong("imageId");
		Image image = getImage(imageId, request);
		if (image!=null) {
			imageService.deleteImage(image, request);
		} else {
			throw new ContentNotFoundException(Image.class,imageId);
		}
	}

	@Path
	public <T extends Entity> void synchronizeMetaData(Request request) throws EndUserException {
		long id = request.getLong("imageId");
		Image image = modelService.getRequired(Image.class, id, request);
		imageService.synchronizeMetaData(image, request);
	}
	
	@Path
	public void changeAccess(Request request) throws SecurityException, ModelException, ContentNotFoundException {
		long imageId = request.getInt("image");
		boolean publicAccess = request.getBoolean("public");
		Image image = getImage(imageId, request);
		if (publicAccess) {
			securityService.makePublicVisible(image, request);
		} else {
			securityService.makePublicHidden(image, request);
		}
		List<Relation> galleryRelations = modelService.find().relations(request).from(ImageGallery.class).to(image).list();
		for (Relation relation : galleryRelations) {
			boolean isPublicGallery = securityService.isPublicView(relation.getFrom(), request);
			if (publicAccess && isPublicGallery) {
				securityService.makePublicVisible(relation, request);
			} else {
				securityService.makePublicHidden(relation, request);
			}
		}
	}
	
	@Path
	public SimpleEntityPerspective createGallery(Request request) throws IOException, EndUserException {
		ImageGallery gallery = imageGalleryService.createGallery(request);
		return SimpleEntityPerspective.create(gallery);
	}

	@Path
	public void uploadToGallery(Request request) throws IOException, EndUserException {
		DataImporter dataImporter = importService.createImporter();
		ImportListener<?> listener = new ImageGalleryImporter(modelService,imageService);
		dataImporter.setListener(listener);
		dataImporter.importMultipart(this, request);
	}

	@Path
	public void updateGalleryTitle(Request request) throws ModelException, SecurityException, ContentNotFoundException {
		long id = request.getInt("id");
		String title = request.getString("title");
		ImageGallery gallery = modelService.getRequired(ImageGallery.class, id, request);
		gallery.setName(title);
		modelService.update(gallery, request);
	}

	@Path
	public <T extends Entity> void deleteGallery(Request request) throws SecurityException, ModelException, ContentNotFoundException {
		long id = request.getLong("id");
		imageGalleryService.deleteGallery(id, request);
	}
	
	@Path
	public void removeImageFromGallery(Request request) throws SecurityException, ModelException, ContentNotFoundException {
		long imageId = request.getLong("imageId");
		long galleryId = request.getLong("galleryId");
		Image image = modelService.getRequired(Image.class, imageId, request);
		ImageGallery gallery = modelService.getRequired(ImageGallery.class, galleryId, request);
		
		Optional<Relation> relation = modelService.getRelation(gallery, image, request);
		if (relation.isPresent()) {
			modelService.delete(relation.get(), request);
		}
	}
	
	@Path
	public void addImagesToGallery(Request request) throws SecurityException, ModelException, ContentNotFoundException, IllegalRequestException {
		GalleryModificationRequest per = request.getObject("info", GalleryModificationRequest.class);
		if (per==null) {
			throw new IllegalRequestException("Malformed data");
		}
		ImageGallery gallery = modelService.getRequired(ImageGallery.class, per.getGalleryId(), request);
		float position = getMaxImagePosition(gallery, request);
		int num = 0;
		for (SimpleEntityPerspective imagePerspective : per.getImages()) {
			Image image = modelService.get(Image.class, imagePerspective.getId(), request);
			if (image!=null) {
				num++;
				Relation relation = new Relation(gallery, image);
				relation.setPosition(position + num);
				modelService.create(relation, request);
				if (securityService.isPublicView(gallery, request) && securityService.isPublicView(image, request)) {
					securityService.grantPublicView(relation, true, request);
				}
			}
		}
	}

	@Path
	public void changeGallerySequence(Request request) throws SecurityException, ModelException, ContentNotFoundException {
		GalleryModificationRequest info = request.getObject("info", GalleryModificationRequest.class);
		List<Long> ids = Lists.newArrayList();
		for (SimpleEntityPerspective image : info.getImages()) {
			ids.add(image.getId());
		}
		imageGalleryService.changeSequence(info.getGalleryId(), ids, request);
	}
	
	private float getMaxImagePosition(Entity gallery, Operator operator) throws ModelException {
		float max = 0;
		List<Relation> relations = modelService.getRelationsFrom(gallery, Image.class, operator);
		for (Relation relation : relations) {
			max = Math.max(max, relation.getPosition());
		}
		return max;
	}
	
	@Path
	public List<Image> imageFinderGallery(Request request) {
		Query<Image> query = Query.of(Image.class).as(request).orderByCreated().descending();
		return modelService.list(query, request);
	}

	@Path
	public ImageMetaData getMetaData(Request request) throws ModelException, SecurityException, IllegalRequestException {
		Long imageId = request.getLong("imageId");
		Image image = modelService.get(Image.class, imageId, request);
		if (image==null) {
			throw new IllegalRequestException("Unabe to load image");
		}
		
		ImageMetaData metaData = imageService.getMetaData(image);
		return metaData;
	}
}
