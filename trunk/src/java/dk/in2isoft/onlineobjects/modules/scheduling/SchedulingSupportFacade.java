package dk.in2isoft.onlineobjects.modules.scheduling;

import dk.in2isoft.onlineobjects.core.ModelService;
import dk.in2isoft.onlineobjects.modules.index.WordIndexer;
import dk.in2isoft.onlineobjects.modules.information.InformationService;
import dk.in2isoft.onlineobjects.modules.surveillance.SurveillanceService;
import dk.in2isoft.onlineobjects.modules.synchronization.MailWatchingService;
import dk.in2isoft.onlineobjects.services.FileService;
import dk.in2isoft.onlineobjects.util.images.ImageService;

public class SchedulingSupportFacade {

	private InformationService informationService;
	private ModelService modelService;
	private FileService fileService;
	private ImageService imageService;
	private MailWatchingService mailWatchingService;
	private SurveillanceService surveillanceService;
	private WordIndexer wordIndexer;
	private SchedulingService schedulingService;

	public InformationService getInformationService() {
		return informationService;
	}

	public void setInformationService(InformationService informationService) {
		this.informationService = informationService;
	}

	public ModelService getModelService() {
		return modelService;
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public FileService getFileService() {
		return fileService;
	}

	public void setFileService(FileService fileService) {
		this.fileService = fileService;
	}

	public ImageService getImageService() {
		return imageService;
	}

	public void setImageService(ImageService imageService) {
		this.imageService = imageService;
	}

	public MailWatchingService getMailWatchingService() {
		return mailWatchingService;
	}

	public void setMailWatchingService(MailWatchingService mailWatchingService) {
		this.mailWatchingService = mailWatchingService;
	}

	public SurveillanceService getSurveillanceService() {
		return surveillanceService;
	}

	public void setSurveillanceService(SurveillanceService surveillanceService) {
		this.surveillanceService = surveillanceService;
	}

	public WordIndexer getWordIndexer() {
		return wordIndexer;
	}

	public void setWordIndexer(WordIndexer wordIndexer) {
		this.wordIndexer = wordIndexer;
	}
	
	public void setSchedulingService(SchedulingService schedulingService) {
		this.schedulingService = schedulingService;
	}
	
	public SchedulingService getSchedulingService() {
		return schedulingService;
	}
}