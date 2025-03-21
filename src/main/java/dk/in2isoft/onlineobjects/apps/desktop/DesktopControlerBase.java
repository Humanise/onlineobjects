package dk.in2isoft.onlineobjects.apps.desktop;

import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dk.in2isoft.onlineobjects.apps.ApplicationController;
import dk.in2isoft.onlineobjects.apps.ApplicationSession;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.modules.networking.HTMLService;
import dk.in2isoft.onlineobjects.services.ImportService;
import dk.in2isoft.onlineobjects.util.images.ImageService;

public abstract class DesktopControlerBase extends ApplicationController {

	protected ImportService importService;
	protected ImageService imageService;
	protected HTMLService htmlService;
	protected SecurityService securityService;
	protected static final Logger log = LogManager.getLogger(DesktopController.class);
	
	public DesktopControlerBase() {
		super("desktop");
	}

	public List<Locale> getLocales() {
		return null;
	}

	@Override
	public ApplicationSession createToolSession() {
		return new DesktopSession();
	}

	public void setImportService(ImportService importService) {
		this.importService = importService;
	}

	public void setImageService(ImageService imageService) {
		this.imageService = imageService;
	}

	public void setHtmlService(HTMLService htmlService) {
		this.htmlService = htmlService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
}