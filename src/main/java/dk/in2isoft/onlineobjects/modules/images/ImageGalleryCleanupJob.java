package dk.in2isoft.onlineobjects.modules.images;

import java.util.Iterator;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.RelationQuery;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.core.exceptions.SecurityException;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.model.ImageGallery;
import dk.in2isoft.onlineobjects.model.Relation;
import dk.in2isoft.onlineobjects.modules.scheduling.JobStatus;
import dk.in2isoft.onlineobjects.modules.scheduling.ServiceBackedJob;

public class ImageGalleryCleanupJob extends ServiceBackedJob {

	public void execute(JobExecutionContext context) throws JobExecutionException {
		ModelService modelService = schedulingSupportFacade.getModelService();
		SecurityService security = schedulingSupportFacade.getSecurityService();
		Operator admin = modelService.newAdminOperator();
		RelationQuery relationQuery = modelService.find().relations(admin).from(ImageGallery.class).to(Image.class);
		JobStatus status = getStatus(context);
		long total = relationQuery.count();
		Iterator<Relation> stream = relationQuery.stream().iterator();
		status.log("Starting image gallery fix of " + total + " relations");
		int i = 0;
		try {
			while (stream.hasNext()) {
				Relation relation = (Relation) stream.next();
				boolean fromIsPublic = security.isPublicView(relation.getFrom(), admin);
				boolean toIsPublic = security.isPublicView(relation.getTo(), admin);
				if (fromIsPublic && toIsPublic) {
					security.makePublicVisible(relation, admin);
				} else {
					security.makePublicVisible(relation, admin);
				}
				status.setProgress(i, total);
				i++;
			}
			admin.commit();
		} catch (ModelException | SecurityException e) {
			admin.rollBack();
			status.error(e.getMessage(), e);
		}
		status.log("Finished image gallery fixup");
	}

}
