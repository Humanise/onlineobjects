package dk.in2isoft.onlineobjects.modules.images;

import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.Query;
import dk.in2isoft.onlineobjects.core.exceptions.EndUserException;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.modules.scheduling.JobStatus;
import dk.in2isoft.onlineobjects.modules.scheduling.ServiceBackedJob;
import dk.in2isoft.onlineobjects.services.FileService;
import dk.in2isoft.onlineobjects.util.images.ImageService;

public class ImageCleanupJob extends ServiceBackedJob {

	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		ModelService modelService = schedulingSupportFacade.getModelService();
		Operator operator = modelService.newAdminOperator();
		ImageService imageService = schedulingSupportFacade.getImageService();
		FileService fileService = schedulingSupportFacade.getFileService();
		List<Image> list = modelService.list(Query.of(Image.class), operator);
		JobStatus status = getStatus(context);
		status.log("Starting image cleanup");
		for (int i = 0; i < list.size(); i++) {
			try {
				Image image = list.get(i);
				String name = image.getName();
				if (name!=null && name.toLowerCase().endsWith(".jpg")) {
					image.setName(fileService.cleanFileName(name));
					modelService.update(image, operator);
				}
				imageService.synchronizeContentType(image, operator);
				imageService.synchronizeMetaData(image, operator);
				operator.commit();
				status.setProgress(i, list.size());
			} catch (EndUserException e) {
				status.error(e.getMessage(), e);
				operator.rollBack();
			}
		}
		status.log("Finished image cleanup");
	}

}
