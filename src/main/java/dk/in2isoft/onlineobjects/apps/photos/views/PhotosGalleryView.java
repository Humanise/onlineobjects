package dk.in2isoft.onlineobjects.apps.photos.views;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.onlineobjects.modules.photos.Photos;
import org.onlineobjects.modules.photos.ScaledImage;
import org.onlineobjects.modules.photos.Size;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

import dk.in2isoft.commons.jsf.AbstractView;
import dk.in2isoft.commons.lang.Numbers;
import dk.in2isoft.commons.lang.Strings;
import dk.in2isoft.onlineobjects.core.Ability;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.UserSession;
import dk.in2isoft.onlineobjects.core.exceptions.NotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.ImageGallery;
import dk.in2isoft.onlineobjects.model.Property;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.ui.jsf.ListModel;
import dk.in2isoft.onlineobjects.ui.jsf.ListModelResult;
import dk.in2isoft.onlineobjects.ui.jsf.model.MasonryItem;
import dk.in2isoft.onlineobjects.util.Dates;

public class PhotosGalleryView extends AbstractView {
	
	private ModelService modelService;
	private Photos photos;
	
	private ImageGallery imageGallery;
	private String title;
	
	private String username;

	private User user;
	
	private boolean modifiable;

	private ListModel<Image> listModel;
	private List<Image> images;
	
	private Date from;
	private Date to;
	private String info;
	private String view;
	private String language;

	public void before(Request request) throws Exception {
		Locale locale = request.getLocale();
		language = locale.getLanguage();
		String[] path = request.getLocalPath();
		long id = Numbers.parseLong(path[2]);
		final UserSession session = request.getSession();
		if (id > 0) {
			imageGallery = modelService.get(ImageGallery.class, id, request);
			if (imageGallery==null) {
				throw new NotFoundException("The gallery does not exist");
			}
			title = imageGallery.getName();
			user = modelService.getOwner(imageGallery, request);
			username = user.getUsername();
			modifiable = user!=null && user.getId()==session.getIdentity() && session.has(Ability.usePhotosApp);

			loadImages(request);
			buildPresentationData();

			listModel = new ListModel<Image>() {

				@Override
				public ListModelResult<Image> getResult() {
					this.setPageSize(images.size());
					return new ListModelResult<Image>(images,images.size());
				}
				
			};
			
			buildInfo(locale);
			
			view = request.getString("view");
			if (Strings.isBlank(view)) {
				view = "grid";
			}
		}
	}

	private void buildInfo(Locale locale) {
		if (from!=null && to!=null) {
			StringBuilder sb = new StringBuilder();
			String fromShort = Dates.formatShortDate(from, locale);
			String toShort = Dates.formatShortDate(to, locale);
			
			sb.append(fromShort);
			if (!fromShort.equals(toShort)) {
				sb.append(" ").append(Strings.RIGHTWARDS_ARROW).append(" ");
				sb.append(toShort);
			}
			info = sb.toString();
		}
	}

	private void loadImages(Operator operator) throws ModelException {
		images = Lists.newArrayList();
		List<Relation> childRelations = modelService.find().relations(operator).from(imageGallery).to(Image.class).list();
		//List<Relation> childRelations = modelService.getRelationsFrom(imageGallery, Image.class, operator);
		for (Relation relation : childRelations) {
			Image image = (Image) relation.getTo();
			Date date = image.getPropertyDateValue(Property.KEY_PHOTO_TAKEN);
			if (date!=null) {
				if (from==null || from.after(date)) {
					from = date;
				}
				if (to==null || to.before(date)) {
					to = date;
				}
			}
			images.add(image);
		}
	}
	
	private String presentationData;
	
	private void buildPresentationData() {
		List<Map<?,?>> data = new ArrayList<>();
		for (Image image : images) {
			Size size = photos.getDisplaySize(image);
			List<ScaledImage> scaledSizes = photos.buildScaledSizes(size, image.getId());
			data.add(Map.of("id", image.getId(),
				"width", size.getWidth(),
				"height", size.getHeight(),
				"sizes", scaledSizes));
		}
		presentationData = Strings.toJSON(data);
	}
	
	public String getPresentationData() {
		return presentationData;
	}
	
	private List<MasonryItem> masonryList;
	
	public List<MasonryItem> getMasonryList() {
		if (masonryList==null) {
			masonryList = Lists.newArrayList();
			masonryList = Lists.newArrayList();
			for (Image image : images) {
				MasonryItem item = new MasonryItem();
				item.id = image.getId();
				item.height = image.getHeight();
				item.width = image.getWidth();
				item.title = image.getName();
				item.href = "/" + language + "/photo/" + item.id + ".html?context=" + imageGallery.getId();
				item.colors = image.getPropertyValue(Property.KEY_PHOTO_COLORS);
				item.rotation = image.getPropertyDoubleValue(Property.KEY_PHOTO_ROTATION);
				if (item.rotation!=null && (item.rotation.intValue()==90 || item.rotation.intValue()==270)) {
					item.height = image.getWidth();
					item.width = image.getHeight();
				}
				masonryList.add(item);
			}
		}
		return masonryList;
	}
	
	public String getView() {
		return view;
	}
	
	public String getInfo() {
		return info;
	}
	
	public Date getFrom() {
		return from;
	}
	
	public Date getTo() {
		return to;
	}
	
	public ListModel<Image> getListModel() {
		return listModel;
	}
	
	public ImageGallery getImageGallery() {
		return imageGallery;
	}
	
	protected User getUser() {
		return user;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getTitle() {
		return title;
	}
	
	public boolean isModifiable() {
		return modifiable;
	}
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
	
	@Autowired
	public void setPhotos(Photos photos) {
		this.photos = photos;
	}
}
