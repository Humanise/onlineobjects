package dk.in2isoft.onlineobjects.apps.people;

import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;

import dk.in2isoft.onlineobjects.apps.ApplicationController;
import dk.in2isoft.onlineobjects.core.Operator;
import dk.in2isoft.onlineobjects.core.SecurityService;
import dk.in2isoft.onlineobjects.core.exceptions.ContentNotFoundException;
import dk.in2isoft.onlineobjects.core.exceptions.ModelException;
import dk.in2isoft.onlineobjects.model.Image;
import dk.in2isoft.onlineobjects.modules.user.MemberService;
import dk.in2isoft.onlineobjects.services.ImportService;
import dk.in2isoft.onlineobjects.ui.Request;
import dk.in2isoft.onlineobjects.util.images.ImageService;

public class PeopleControllerBase extends ApplicationController {

	protected ImageService imageService;
	protected SecurityService securityService;
	protected ImportService importService;
	protected MemberService memberService;

	public PeopleControllerBase() {
		super("people");
	}
		
	public List<Locale> getLocales() {
		return Lists.newArrayList(new Locale("en"),new Locale("da"));
	}

	@Override
	public String getLanguage(Request request) {
		String[] path = request.getLocalPath();
		if (path.length>0) {
			if ("en".equals(path[0]) || "da".equals(path[0])) {
				return path[0];
			}
		}
		return super.getLanguage(request);
	}

	protected Image getImage(long id, Operator privileged) throws ModelException, ContentNotFoundException {
		Image image = modelService.get(Image.class, id,privileged);
		if (image==null) {
			throw new ContentNotFoundException("The image was not found");
		}
		return image;
	}
	
	public void setImageService(ImageService imageService) {
		this.imageService = imageService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	
	public void setImportService(ImportService importService) {
		this.importService = importService;
	}
	
	public void setMemberService(MemberService memberService) {
		this.memberService = memberService;
	}
}