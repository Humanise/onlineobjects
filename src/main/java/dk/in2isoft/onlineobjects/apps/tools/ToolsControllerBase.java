package dk.in2isoft.onlineobjects.apps.tools;

import dk.in2isoft.onlineobjects.apps.ApplicationController;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.modules.information.InformationService;
import dk.in2isoft.onlineobjects.modules.user.InvitationService;
import dk.in2isoft.onlineobjects.services.ImportService;
import dk.in2isoft.onlineobjects.services.PersonService;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.util.images.ImageService;

public abstract class ToolsControllerBase extends ApplicationController {

	protected ImportService importService;
	protected ImageService imageService;
	protected InformationService informationService;
	protected InvitationService invitationService;
	protected PersonService personService;
	protected SecurityService securityService;
	
	public ToolsControllerBase() {
		super("tools");
	}

	@Override
	public String getLanguage(Request request) {
		String[] path = request.getLocalPath();
		if (path.length>0) {
			return path[0];
		}
		return super.getLanguage(request);
	}

	public void setImportService(ImportService importService) {
		this.importService = importService;
	}
	
	public void setImageService(ImageService imageService) {
		this.imageService = imageService;
	}
	
	public void setInformationService(InformationService informationService) {
		this.informationService = informationService;
	}
	
	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}
	
	public void setInvitationService(InvitationService invitationService) {
		this.invitationService = invitationService;
	}
	
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
}