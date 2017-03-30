package dk.in2isoft.onlineobjects.apps.photos.views;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;

import com.google.common.collect.Lists;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Privileged;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.SearchResult;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.User;
import dk.in2isoft.onlineobjects.modules.photos.SimplePhotoPerspective;
import dk.in2isoft.onlineobjects.modules.photos.SimplePhotoQuery;
import dk.in2isoft.onlineobjects.ui.AbstractManagedBean;
import dk.in2isoft.onlineobjects.ui.jsf.ListModel;
import dk.in2isoft.onlineobjects.ui.jsf.ListModelResult;
import dk.in2isoft.onlineobjects.ui.jsf.model.GalleryItem;
import dk.in2isoft.onlineobjects.ui.jsf.model.MasonryItem;

public class PhotosFrontView extends AbstractManagedBean implements InitializingBean {

	private ModelService modelService;
	private SecurityService securityService;
	private List<MasonryItem> masonryList;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		SimplePhotoQuery simplePhotoQuery = new SimplePhotoQuery(securityService.getPublicUser());
		List<SimplePhotoPerspective> list = modelService.list(simplePhotoQuery);
		String language = getRequest().getLanguage();
		
		
		masonryList = Lists.newArrayList();
		for (SimplePhotoPerspective image : list) {
			MasonryItem item = new MasonryItem();
			item.id = image.getId();
			item.height = image.getHeight();
			item.width = image.getWidth();
			item.title = image.getTitle();
			item.href = "/" + language + "/photo/" + item.id + ".html";
			item.rotation = image.getRotation();
			if (item.rotation!=null && (item.rotation.intValue()==90 || item.rotation.intValue()==270)) {
				item.height = image.getWidth();
				item.width = image.getHeight();
			}
			item.colors = image.getColors();
			masonryList.add(item);
		}
	}
	
	public ListModel<GalleryItem> getImageList() {
		ListModel<GalleryItem> model = new ListModel<GalleryItem>() {

			@Override
			public ListModelResult<GalleryItem> getResult() {
				Query<Image> query = Query.of(Image.class).orderByCreated().withPaging(getPage(), getPageSize()).as(securityService.getPublicUser()).descending();
				SearchResult<Image> result = modelService.search(query);
				List<GalleryItem> list = convert(result.getList(), getRequest().getSession());
				return new ListModelResult<GalleryItem>(list,result.getTotalCount());
			}
			
		};
		model.setPageSize(40);
		return model;
	}
	
	private List<GalleryItem> convert(List<Image> images, Privileged privileged) {
		List<GalleryItem> list = new ArrayList<GalleryItem>();
		for (Image image : images) {
			User user = null;
			try {
				user = modelService.getOwner(image, privileged);
			} catch (ModelException ignore) {}
			list.add(GalleryItem.create(image, user));
		}		
		return list;
	}
	
	public List<MasonryItem> getMasonryList() {
		return masonryList;
	}

	// Wiring...
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public ModelService getModelService() {
		return modelService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public SecurityService getSecurityService() {
		return securityService;
	}
}
