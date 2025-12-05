package dk.in2isoft.onlineobjects.apps.photos.views;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.onlineobjects.modules.photos.Photos;
import org.onlineobjects.modules.photos.ScaledImage;
import org.onlineobjects.modules.photos.Size;

import com.google.common.collect.Lists;

import dk.in2isoft.commons.jsf.AbstractView;
import dk.in2isoft.commons.lang.Files;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.apps.photos.PhotosController;
import dk.in2isoft.onlineobjects.core.Ability;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Pair;
import dk.in2isoft.onlineobjects.core.PairSearchResult;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.UsersPersonQuery;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.ImageGallery;
import dk.in2isoft.onlineobjects.model.Location;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.model.Word;
import dk.in2isoft.onlineobjects.services.PersonService;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.ui.jsf.model.GalleryLink;
import dk.in2isoft.onlineobjects.ui.jsf.model.MapPoint;
import dk.in2isoft.onlineobjects.util.Dates;
import dk.in2isoft.onlineobjects.util.Messages;
import dk.in2isoft.onlineobjects.util.images.ImageInfo;
import dk.in2isoft.onlineobjects.util.images.ImageService;
import dk.in2isoft.onlineobjects.util.images.ImageTransformation;

public class PhotosPhotoView extends AbstractView {

	private static final Logger log = LogManager.getLogger(PhotosPhotoView.class);

	private ModelService modelService;
	private SecurityService securityService;
	private ImageService imageService;
	private PersonService personService;
	private Photos photos;

	private String language;
	private Image image;
	private ImageInfo imageInfo;
	private Location location;
	private MapPoint mapPoint;
	private User user;
	private Person person;
	private Image personImage;
	private boolean secret;
	private boolean canModify;
	private List<SelectItem> properties;
	private Long nextId;
	private Long previousId;
	private List<Pair<Word,String>> words;
	private boolean rotated;
	private List<ImageGallery> galleries;
	private List<GalleryLink> galleryLinks;
	private Long context;

	private String fullPersonName;
	private Long imageId;
	private boolean featured;
	private String colors;
	private String taken;

	private Size size;

	public void before(Request request) throws Exception {
		{
			String[] path = request.getLocalPath();
			String string = path[path.length-1];
			String[] split = string.split("\\.");
			imageId = Long.valueOf(split[0]);
		}
		StopWatch watch = new StopWatch();
		watch.start();
		image = modelService.get(Image.class, imageId, request);
		trace(watch, "Image loaded");
		if (image!=null) {
			Messages msg = new Messages(PhotosController.class);
			if (!securityService.canView(image, request)) {
				image = null;
				return;
			}
			trace(watch, "Can view");

			imageInfo = imageService.getImageInfo(image, request);
			trace(watch, "Get image info");

			rotated = imageInfo.isRotated();
			this.size = photos.getDisplaySize(image);
			trace(watch, "Sizes");

			canModify = securityService.canModify(image, request) && request.getSession().has(Ability.usePhotosApp);
			trace(watch, "canModify");

			if (canModify) {
				secret = !securityService.canView(image, request.as(securityService.getPublicUser()));
			}
			buildSizes();


			location = modelService.getParent(image, Location.class, request);

			user = modelService.getOwner(image, request);
			if (user!=null) {
				UsersPersonQuery query = new UsersPersonQuery().withUsername(user.getUsername());
				PairSearchResult<User,Person> searchPairs = modelService.searchPairs(query, request);
				Pair<User,Person> first = searchPairs.getFirst();
				if (first!=null) {
					user = first.getKey();
					person = first.getValue();
					personImage = modelService.getChild(user, Relation.KIND_SYSTEM_USER_IMAGE, Image.class, request);
					fullPersonName = personService.getFullPersonName(person, 14);
				}
			}
			buildGalleries(request);
			List<Long> ids = getContextIds(request);
			if (ids.size() > 1) {
				int position = ids.indexOf(image.getId());
				int previous = position > 0 ? position - 1 : ids.size() - 1;
				int next = position < ids.size() - 1 ? position + 1 : 0;
				nextId = ids.get(next);
				previousId = ids.get(previous);
			}

			Locale locale = request.getLocale();
			properties = Lists.newArrayList();
			properties.add(new SelectItem(image.getWidth()+" x "+image.getHeight()+" - "+getMegaPixels()+" Megapixel",msg.get("size", locale)));
			properties.add(new SelectItem(Files.formatFileSize(image.getFileSize())+", "+image.getContentType(),msg.get("file", locale)));
			properties.add(new SelectItem(Dates.formatMediumDate(image.getCreated(),locale ),msg.get("added", locale)));
			if (imageInfo.getTaken()!=null) {
				properties.add(new SelectItem(Dates.formatMediumDate(imageInfo.getTaken(), locale), msg.get("date", locale)));
				this.taken = Dates.formatMediumDate(imageInfo.getTaken(), locale);
			}
			String camera = photos.getCombinedCamera(imageInfo);
			if (Strings.isNotBlank(camera)) {
				properties.add(new SelectItem(camera, "Camera"));
			}
			if (canModify) {
				if (imageInfo.getRotation()!=null) {
					properties.add(new SelectItem(imageInfo.getRotation(),"Rotation"));
				}
			}
			if (location!=null) {
				mapPoint = new MapPoint();
				mapPoint.setTitle(location.getName());
				mapPoint.setLatitude(location.getLatitude());
				mapPoint.setLongitude(location.getLongitude());
			}
			List<Word> wordChildren = modelService.getChildren(image, null, Word.class, request);
			words = Lists.newArrayList();
			for (Word word : wordChildren) {
				String link = "";
				if (user!=null) {
					link = "/"+locale.getLanguage()+"/users/"+user.getUsername()+"/?wordId="+word.getId();
				}
				words.add(Pair.of(word, link));
			}

			String[] path = request.getLocalPath();
			language = path[0];


			featured = imageService.isFeatured(image, request);

			this.colors = image.getPropertyValue(Property.KEY_PHOTO_COLORS);

			buildPreview();
		}
	}

	private void buildGalleries(Request request) throws ModelException {
		galleries = modelService.getParents(image, ImageGallery.class, request);
		galleryLinks = new ArrayList<>();
		for (ImageGallery imageGallery : galleries) {
			int[] ids = modelService.find().relations(request).from(imageGallery).to(Image.class).stream().map(r -> r.getTo().getId()).mapToInt(Long::intValue).toArray();
			GalleryLink link = new GalleryLink();
			link.id = imageGallery.getId();
			link.title = imageGallery.getName();
			link.photoCount = ids.length;
			link.photoIds = ids;
			galleryLinks.add(link);
		}
	}

	private void trace(StopWatch watch, String string) {
		log.info(watch.getTime() + ": " + string);
		watch.reset();
	}

	private List<Long> getContextIds(Request request) throws ModelException {
		context = request.getId("context", null);
		if (context != null) {
			for (GalleryLink galleryLink : galleryLinks) {
				if (galleryLink.id == context) {
					return Arrays.stream(galleryLink.photoIds).asLongStream().boxed().collect(toList());
				}
			}

			ImageGallery gallery = modelService.get(ImageGallery.class, context, request);
			if (gallery != null) {
				List<Relation> relations = modelService.find().relations(request).from(gallery).to(Image.class).list();
				return relations.stream().sorted((a,b) -> Float.compare(a.getPosition(), b.getPosition())).map(r -> r.getTo().getId()).collect(toList());
			}
		}
		Query<Image> query = Query.after(Image.class).as(user).orderByCreated();
		if (user==null || user.getId() != request.getIdentity()) {
			query.withPublicView();
		}
		return modelService.listIds(query, request);
	}

	private void buildSizes() {
		List<ScaledImage> sizes = photos.buildScaledSizes(size, imageId);
		pictureSources = Lists.newArrayList(sizes);
		Collections.reverse(pictureSources);
		this.sizes = Strings.toJSON(sizes);
		this.imageSizes = sizes.stream().map(s -> s.getWidth()).distinct().sorted(Collections.reverseOrder()).map(s -> "(min-width: " + s + "px) " + s + "px").collect(Collectors.joining(", "));
		imageSourceSet = photos.asSourceSet(sizes);
	}

	private List<ScaledImage> pictureSources;

	public List<ScaledImage> getPictureSources() {
		return pictureSources;
	}

	private String sizes;

	private String imageSizes;

	private String imageSourceSet;

	public String getImageSourceSet() {
		return imageSourceSet;
	}

	public String getImageSizes() {
		return imageSizes;
	}

	public String getSizes() {
		return this.sizes;
	}

	public String getNextUrl() {
		return urlInContext(nextId);
	}

	public String getPreviousUrl() {
		return urlInContext(previousId);
	}

	private String urlInContext(Long id) {
		if (id == null) return null;
		StringBuilder sb = new StringBuilder();
		sb.append("/").append(language).append("/photo/").append(id).append(".html");
		if (context != null) {
			sb.append("?context=").append(context);
		}
		return sb.toString();
	}

	private String preview;

	public String getPreview() {
		return preview;
	}

	public void buildPreview() {
		ImageTransformation transform = new ImageTransformation();
		transform.setWidth(100);
		transform.setHeight(100);
		if (imageInfo.getRotation() != null) {
			transform.setRotation(imageInfo.getRotation().floatValue());
		}
		preview = imageService.base64DataUrl(image, transform);
	}

	public boolean isFeatured() {
		return featured;
	}

	public List<SelectItem> getProperties() {
		return properties;
	}
	/*
	public List<ImageGallery> getGalleries() {
		return galleries;
	}*/

	public List<GalleryLink> getGalleryLinks() {
		return galleryLinks;
	}

	public Person getPerson() {
		return person;
	}

	public String getFullPersonName() {
		return fullPersonName;
	}

	public User getUser() {
		return user;
	}

	public Image getImage() {
		return image;
	}

	public boolean isSecret() {
		return secret;
	}

	public ImageInfo getImageInfo() {
		return imageInfo;
	}

	public Long getContext() {
		return context;
	}

	public long getNextId() {
		return nextId;
	}

	public long getPreviousId() {
		return previousId;
	}

	public String getTitle() {
		if (Strings.isBlank(image.getName())) {
			return "No title";
		}
		return image.getName();
	}

	public String getDescription() {
		return StringUtils.trim(imageInfo.getDescription());
	}

	public Location getLocation() {
		return location;
	}

	public MapPoint getMapPoint() {
		return mapPoint;
	}

	public double getMegaPixels() {
		return Math.round(image.getWidth()*image.getHeight()/(double)10000)/(double)100;
	}

	public List<Pair<Word, String>> getWords() {
		return words;
	}

	public Image getPersonImage() {
		return personImage;
	}

	public Long getImageId() {
		return imageId;
	}

	public String getLanguage() {
		return language;
	}

	public boolean isCanModify() {
		return canModify;
	}

	public String getColors() {
		return colors;
	}

	public String getTaken() {
		return taken;
	}

	public boolean isRotated() {
		return rotated;
	}

	public Size getSize() {
		return size;
	}

	// Wiring...

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setImageService(ImageService imageService) {
		this.imageService = imageService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

	public void setPhotos(Photos helper) {
		this.photos = helper;
	}
}
