package dk.in2isoft.onlineobjects.apps.photos.views;

import java.util.List;

import com.google.common.collect.Lists;

import dk.in2isoft.commons.jsf.AbstractView;
import dk.in2isoft.onlineobjects.core.Ability;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Pair;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.UserSession;
import dk.in2isoft.onlineobjects.core.UsersPersonQuery;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.ImageGallery;
import dk.in2isoft.onlineobjects.model.Person;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.services.PersonService;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.ui.data.Option;

public class PhotosLayoutView extends AbstractView {

	private ModelService modelService;
	private PhotosGalleryView photosGalleryView;
	private PersonService personService;
	
	private String username;
	private String fullPersonName;
	private User user;
	private Person person;

	private List<Option> galleries;
	
	private boolean allImages;

	private boolean modifiable;

	private Image userImage;
	
	public void before(Request request) throws Exception {
		String[] path = request.getLocalPath();
		String type = path[1];
		long selected = 0l;
		if ("users".equals(type)) {
			username = path[2];
			allImages = true;
		} else if ("gallery".equals(type)) {
			selected = photosGalleryView.getImageGallery().getId();
			username = photosGalleryView.getUser().getUsername();
		}
		UsersPersonQuery query = new UsersPersonQuery().withUsername(username);
		Pair<User, Person> pair = modelService.searchPairs(query, request).getFirst();
		if (pair == null) {
			throw new ContentNotFoundException("User not found");
		}
		this.user = pair.getKey();
		galleries = Lists.newArrayList();
		this.person = pair.getValue();
		fullPersonName = personService.getFullPersonName(person, 14);
		if (user!=null) {
			Query<ImageGallery> galleryQuery = Query.after(ImageGallery.class).as(user);
			UserSession session = request.getSession();
			if (user.getId() != session.getIdentity()) {
				galleryQuery.withPublicView();
			}
			List<ImageGallery> imageGalleries = modelService.list(galleryQuery, request);
			for (ImageGallery gallery : imageGalleries) {
				Option option = Option.of(gallery.getName(), gallery.getId());
				option.setSelected(gallery.getId()==selected);
				galleries.add(option);
			}
			modifiable = this.user.getId() == session.getIdentity() && session.has(Ability.usePhotosApp);
			userImage = modelService.getChild(user, Relation.KIND_SYSTEM_USER_IMAGE, Image.class, request);
		}
	}
	
	public boolean isAllImages() {
		return allImages;
	}
	
	public Image getUserImage() {
		return userImage;
	}
	
	public boolean isModifiable() {
		return modifiable;
	}
	
	public List<Option> getGalleries() {
		return galleries;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPersonName() {
		return fullPersonName;
	}
	
	// Wiring...

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
	public void setPhotosGalleryView(PhotosGalleryView photosGalleryView) {
		this.photosGalleryView = photosGalleryView;
	}
	
	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}
}
