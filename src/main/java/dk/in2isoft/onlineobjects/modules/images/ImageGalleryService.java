package dk.in2isoft.onlineobjects.modules.images;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

import dk.in2isoft.commons.lang.Code;
import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.exceptions.NotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Entity;
import dk.in2isoft.onlineobjects.model.HeaderPart;
import dk.in2isoft.onlineobjects.model.HtmlPart;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.ImageGallery;
import dk.in2isoft.onlineobjects.model.Relation;

public class ImageGalleryService {

	private ModelService modelService;
	
	public ImageGallery createGallery(Operator priviledged) throws EndUserException {

		// Create an image gallery
		ImageGallery gallery = new ImageGallery();
		gallery.setName("Mine billeder");
		modelService.create(gallery, priviledged);

		// Create gallery title
		HeaderPart header = new HeaderPart();
		header.setText("Mine billeder");
		modelService.create(header, priviledged);
		modelService.createRelation(gallery, header, priviledged);

		// Create gallery title
		HtmlPart text = new HtmlPart();
		text.setHtml("Dette er nogle billeder jeg har taget");
		modelService.create(text, priviledged);
		modelService.createRelation(gallery, text, priviledged);

		return gallery;
	}

	public <T extends Entity>void deleteGallery(long id, Operator privileged) throws ModelException, SecurityException, NotFoundException {
		ImageGallery gallery = modelService.get(ImageGallery.class, id, privileged);
		if (gallery==null) {
			throw new NotFoundException(ImageGallery.class, id);
		}
		List<Class<T>> parts = Lists.newArrayList();
		parts.add(Code.<Class<T>>cast(HtmlPart.class));
		parts.add(Code.<Class<T>>cast(HeaderPart.class));
		for (Class<T> type : parts) {
			List<T> relations = modelService.getChildren(gallery, type, privileged);
			for (T relation : relations) {
				modelService.delete(relation, privileged);
			}
		}
		modelService.delete(gallery, privileged);
	}
	
	public void changeSequence(long galleryId, final List<Long> imageIds, Operator privileged) throws ModelException, NotFoundException, SecurityException {
		ImageGallery gallery = modelService.getRequired(ImageGallery.class, galleryId, privileged);
		
		List<Relation> relations = modelService.getRelationsFrom(gallery, Image.class, privileged);
		Collections.sort(relations, new Comparator<Relation>() {

			@Override
			public int compare(Relation o1, Relation o2) {
				int index1 = imageIds.indexOf(o1.getTo().getId());
				int index2 = imageIds.indexOf(o2.getTo().getId());
				return index1-index2;
			}
		});
		float pos = 1;
		for (Relation relation : relations) {
			if (relation.getPosition()!=pos) {
				relation.setPosition(pos);
				modelService.update(relation, privileged);
			}
			pos++;
		}
	}

	// Wiring...
	
	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}
}
