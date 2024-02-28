package dk.in2isoft.onlineobjects.modules.images;

import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.ImageGallery;
import dk.in2isoft.onlineobjects.model.Location;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.modules.scheduling.JobBase;
import dk.in2isoft.onlineobjects.modules.scheduling.JobStatus;
import dk.in2isoft.onlineobjects.services.FileService;
import dk.in2isoft.onlineobjects.util.images.ImageService;

public class ImageCleanupJob extends JobBase {

	@Autowired
	ModelService model;

	@Autowired
	ImageService images;
	
	@Autowired
	FileService files;
	
	@Autowired
	SecurityService securityService;
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Operator operator = model.newAdminOperator();
		List<Image> list = model.list(Query.of(Image.class), operator);
		JobStatus status = getStatus(context);
		status.log("Starting image cleanup");
		for (int i = 0; i < list.size(); i++) {
			try {
				Image image = list.get(i);
				String name = image.getName();
				if (name!=null && name.toLowerCase().endsWith(".jpg")) {
					image.setName(files.cleanFileName(name));
					model.update(image, operator);
				}
				images.synchronizeContentType(image, operator);
				images.synchronizeMetaData(image, operator);
				fixAccess(image, operator);
				operator.commit();
				status.setProgress(i, list.size());
			} catch (EndUserException e) {
				status.error(e.getMessage(), e);
				operator.rollBack();
			}
		}
		status.log("Finished image cleanup");
	}

	private void fixAccess(Image image, Operator request) throws ModelException, SecurityException {
		boolean publicAccess = securityService.isPublicView(image, request);
		Location location = model.getParent(image, Location.class, request);
		if (location != null) {
			if (publicAccess) {
				securityService.makePublicVisible(location, request);
			} else {
				securityService.makePublicHidden(location, request);
			}			
		}
		List<Relation> galleryRelations = model.find().relations(request).from(ImageGallery.class).to(image).list();
		for (Relation relation : galleryRelations) {
			boolean isPublicGallery = securityService.isPublicView(relation.getFrom(), request);
			if (publicAccess && isPublicGallery) {
				securityService.makePublicVisible(relation, request);
			} else {
				securityService.makePublicHidden(relation, request);
			}
		}
	}
}
